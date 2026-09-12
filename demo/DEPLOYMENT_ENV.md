# Required deployment environment variables

Set these outside source control before starting LogiTrack:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_JWT_SECRET` (at least 32 characters)
- `SPRING_MAIL_USERNAME` (if email is enabled)
- `SPRING_MAIL_PASSWORD` (mail app password, if email is enabled)

Optional:

- `PORT` (defaults to `8090`)
- `SPRING_JPA_HIBERNATE_DDL_AUTO` (defaults to `update`)
- `SPRING_JPA_SHOW_SQL` (defaults to `false`)
- `SPRING_MAIL_HOST` (defaults to `smtp.gmail.com`)
- `SPRING_MAIL_PORT` (defaults to `587`)

Do not commit real database passwords, JWT secrets, or mail app passwords.
