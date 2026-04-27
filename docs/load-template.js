
// Ejecutar con: node load-templates.js

import fs from 'node:fs';

const TOKEN = 'eyJraWQiOiJnb2Etand0IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiI2NDcxNTk1NjgiLCJhdWQiOiJzcGFDbGllbnRJZCIsIm5iZiI6MTc3NTU4MjUxNiwic2NvcGUiOlsib3BlbmlkIiwicHJvZmlsZSIsIm9mZmxpbmVfYWNjZXNzIl0sInJvbGVzIjoiYWRtaW4iLCJpc3MiOiJodHRwczovL2dlc3Rpb24ub2NhbmFib2dhZG9zLmVzL2FwaS9nb2EtdXNlciIsIm5hbWUiOiJKZXPDunMiLCJleHAiOjE3NzU2MTg1MTYsImlhdCI6MTc3NTU4MjUxNiwianRpIjoiZTRiZjliMzMtNmJkNS00MmY4LWFkOWUtYTBlMjAzZDNiZDMwIn0.Jno18wr37D0FapEHRbt-7ldKw9WpQNI8jFTPZauK3Lgp8tKEjgbPQv3lfArA6P2X5vDCu4UKQhjwr7YwXHc7XnhX1TEs0tvfdaPHykn5hl5YJ03agsAxrwV5Io_Jo17ZXydt1WE4wQ-lIqt3MVfiKRwp0jZInyHOw_JnP9Rwpy5Q8MLDX1jDf8YLe3QHX3Zf1GD0oZpRn06koveJeGqEMroqIaSciv_nqEiXGNrjnt5ooq3u_ohSDNa4BiBLeddgFuL_TYZyFIR7uM8TjU3QpFOdfCfLTtAd9I6Q4Ks89eujtKAw4i04Foh0may23YTj1a0LdDZJQ0sNaRmSC2OerQ';
const API = 'https://gestion.ocanabogados.es/api/goa-engagement';
const templates = JSON.parse(fs.readFileSync('templates.json', 'utf8'));

async function post(url, body) {
  const r = await fetch(url, {
    method: 'POST',
    headers: {Authorization: 'Bearer ' + TOKEN, 'Content-Type': 'application/json; charset=utf-8', },
    body: JSON.stringify(body),
  });
  return r.status;
}

for (const template of templates) {
  for (const task of template.legalTasks) {
    console.log('Task: ' + task.title + ' - HTTP ' + await post(API + '/legal-tasks', task));
  }
  console.log('Template: ' + template.title + ' - HTTP ' + await post(API + '/legal-procedure-templates', template),);
}
