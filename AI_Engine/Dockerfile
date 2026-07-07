# Use official Python image
FROM python:3.12-slim

# Set the working directory
WORKDIR /app

# Copy the requirements file and install dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of the code
COPY . .

# Expose port 5000 for Flask
EXPOSE 5000

# Command to run the Flask server
CMD ["flask", "run", "--host=0.0.0.0", "--port=5000"]