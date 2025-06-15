# OpenTripPlanner Helsinki Setup Guide

> Setting this up properly was difficult, so I decided to write a guide in case anyone else ever needs to run this (
> hopefully you don’t).  
> I based this on the [official OTP tutorial](https://docs.opentripplanner.org/en/latest/Basic-Tutorial/#get-some-data),
> but I had to do several extra steps.

## Overview

The goal is to run the OTP server with Java. Once it's running, the Routing Comparator class can connect to it and
compare the OTP algorithm with ours.

---

## Step 1: Download OTP

```bash
wget https://repo1.maven.org/maven2/org/opentripplanner/otp/2.3.0/otp-2.3.0-shaded.jar
```

---

## Step 2: Create directory for data

```bash
mkdir helsinki-data && cd helsinki-data
```

---

## Step 3: Download OSM Data

```bash

Add your gtfs zip file in this directory (OTP requires the filename to include `.gtfs.zip`)

# Download OSM data for Finland
wget https://download.geofabrik.de/europe/finland/osm/finland-latest.osm.pbf
```

I had to use a subset of Helsinki. You might need to do the same:

```bash
osmium extract -b 24.7,60.1,25.3,60.3 finland-latest.osm.pbf -o helsinki-proper.osm.pbf
```

---

## Step 4: Build and Run OTP

```bash
java -Xmx4G -jar ../otp-2.3.0-shaded.jar --build --serve .
```

---

## Troubleshooting

- **Error with `agency.txt`:** You may need to delete the `agency.txt` file from your GTFS zip to avoid issues.
- **Custom Build Config:** You might need a `build-config.json` file with the following content:

```json
{
  "transitServiceStart": "2024-01-01",
  "transitServiceEnd":   "2024-12-31"
}
```

---
