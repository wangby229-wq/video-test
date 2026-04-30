#!/bin/bash
sed -i 's|src="/|src="|g' /opt/wechat_meeting/dist/index.html
sed -i 's|href="/|href="|g' /opt/wechat_meeting/dist/index.html
cat /opt/wechat_meeting/dist/index.html
