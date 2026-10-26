# Collection backup import

Send an authenticated `POST` request to `/mtg-collectron/importbackup` as `multipart/form-data` with:

* `file`: the collection backup JSON file
* `userId`: the ID of the account being verified
* `password`: that account's password

This endpoint does not require a CSRF token. It still requires an authenticated session and verifies the supplied user ID and password before transactionally importing each backed-up set into `user_set_info` and each card into `user_cards`. Records with matching user/set/collector-number keys are updated; other existing collection records are left unchanged. A successful response includes `setsImported` and `cardsImported`.

The backup format contains card names up to 141 characters, so ensure the database column can store them before importing:

```sql
ALTER TABLE user_cards MODIFY card_name VARCHAR(255) NOT NULL;
```
