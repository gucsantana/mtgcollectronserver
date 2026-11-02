# AWS deployment

This project can be deployed as a Java 25 container on Amazon ECS with AWS
Fargate, an Application Load Balancer (ALB), and Amazon RDS for MySQL. The
GitHub Actions workflow builds and deploys the application; create the AWS
resources and configure the GitHub settings below before enabling it.

## 1. Prepare the MySQL database

1. Create an RDS for MySQL instance in a private subnet. Set its database name
   to `mtg_collectron`.
2. Allow inbound MySQL traffic on port 3306 only from the ECS task security
   group. Do not make the database publicly accessible.
3. Before moving existing user or collection data, take a backup and migrate
   the existing schema and data to RDS.

There are no database migration scripts in this repository. Do not rely on
Hibernate to create or alter the production schema; migrate and verify the
existing schema before deploying. Add versioned migrations when the schema
change history is available.

## 2. Create the container and ECS resources

1. Create an ECR repository named `mtgcollectronserver`.
2. Create an ECS cluster and a Fargate task definition using Linux/x86_64.
   The task definition must have a container named `mtgcollectronserver`,
   listen on container port 8080, and use an execution role that can pull from
   ECR and write logs to CloudWatch.
3. Configure the container environment:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `COLLECTRON_DATASOURCE_URL=jdbc:mysql://<RDS-ENDPOINT>:3306/mtg_collectron`
   - `DB_USER` and `DB_PASS` from Secrets Manager
   - `SCRYFALL_IMPORT_TOKEN` from Secrets Manager
4. Put the ECS service in private subnets, allow its security group to receive
   traffic from the ALB security group on port 8080, and give it outbound
   network access if it needs to reach Scryfall.
5. Add an ALB target group health check for
   `/mtg-collectron/actuator/health`. The ECS service should register its task
   with that target group.
6. Request or import an ACM certificate, configure an HTTPS listener on the
   ALB, and redirect HTTP to HTTPS. Add a DNS record for your domain if you
   have one.

The production profile enables secure, HTTP-only session cookies and trusts
forwarded headers from the load balancer. Actuator exposes only the health
endpoint without component details so the ALB can probe it; application routes
remain protected by the existing authentication rules.

## 3. Configure GitHub Actions

The workflow in `.github/workflows/deploy-aws.yml` runs tests, builds and pushes
an image tagged with the commit SHA, then updates the ECS task definition and
service. It uses GitHub OIDC; do not put long-lived AWS access keys in GitHub.

1. Configure an AWS IAM OIDC provider for GitHub Actions and a role trusted
   only by this repository. Grant that role permissions to push to the ECR
   repository, describe/register ECS task definitions, update the ECS service,
   and pass the ECS task and execution roles.
2. Add the following **repository variables** under GitHub Actions settings:
   - `AWS_REGION`: region containing the AWS resources.
   - `ECR_REPOSITORY`: `mtgcollectronserver`.
   - `ECS_CLUSTER`: ECS cluster name.
   - `ECS_SERVICE`: ECS service name.
   - `ECS_TASK_DEFINITION`: task-definition family or ARN.
   - `ECS_CONTAINER_NAME`: `mtgcollectronserver`.
3. Add the **repository secret** `AWS_ROLE_TO_ASSUME` with the IAM role ARN.
4. Push to `main` or run the workflow manually. Wait for the ECS service to
   become stable, then verify the ALB health check and test login, CSRF-
   protected requests, and database operations over HTTPS.

## 4. Before scaling beyond one task

Start with one ECS task. Authentication sessions and Scryfall import status
are held in application memory, so multiple tasks can lose session continuity
or report different import states. Use shared session storage and shared
coordination for import jobs before running multiple tasks. Review cache
behavior as well, since the current caches are local to each process.
