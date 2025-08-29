# File Storage Application

This is a file storage application that provides functionalities to manage files and directories in a cloud storage
environment. The application includes features such as uploading, downloading, moving, renaming, and deleting files and
directories. 

## Features

- **User Registration**: Allows users to register using username and password.
- **File Management**:
    - Upload and download files to and from the server.
    - Create, move, rename, and delete files and directories.
    - List files and directories recursively.
- **Error Handling**: Custom exceptions for various error scenarios such as file not found, directory already exists,
  etc.

## Technologies Used

- **Spring Boot**: For the backend application framework.
- **PostgreSQL**: As the relational database to store file and user metadata.
- **MinIO**: For file storage (acts as an S3-compatible object storage).
- **Spring Security**: For authentication and authorization.

## Setup Instructions

1. Clone the repository:

   ```bash
   git clone https://github.com/kivislime/file-storage-app.git
   cd file-storage-app

2. Set up your environment variables:

Create an env file in the root directory or modify an existing one and add your own settings or the following:

```bash
POSTGRES_DB=testdb
POSTGRES_USER=test
POSTGRES_PASSWORD=test
MINIO_ROOT_USER=minio
MINIO_ROOT_PASSWORD=minio
MINIO_BUCKET_NAME=mybucket
```

3. Make sure Docker is running, then:
```bash
docker-compose up --build
```
