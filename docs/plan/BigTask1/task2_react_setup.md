# Task 2: React Frontend Initialization

## Description
Set up the separate React frontend project.

## What we should implement
- Initialize via `npm create vite@latest frontend --template react` or CRA.
- Install dependencies: `axios`, `react-router-dom`, `tailwindcss` (or Bootstrap for UI), `jwt-decode`.

## How to execute
- Configure a base Axios instance with an interceptor to automatically attach the JWT `Authorization: Bearer <token>` to all requests.