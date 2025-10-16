FROM node:20 as build
LABEL authors="anasl"

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci --silent

# Copy application source
COPY . .

# Build the Angular application
RUN npm run build

# Use nginx as the production server
FROM nginx:

# Copy the built application from the build stage
COPY --from=build /app/dist/angular-app/browser /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
