REASON_CODES = {
    'common': [
        {'code': 'INAPPROPRIATE', 'label': 'Inappropriate content'},
        {'code': 'MISINFORMATION', 'label': 'Misinformation'},
        {'code': 'SPAM', 'label': 'Spam'},
        {'code': 'HARASSMENT', 'label': 'Harassment'},
        {'code': 'OTHER', 'label': 'Other'},
    ],
    'space': [
        {'code': 'DUPLICATE_SPACE', 'label': 'Duplicate space'},
        {'code': 'MISLEADING_TITLE_OR_DESCRIPTION', 'label': 'Misleading title or description'},
    ],
    'node': [
        {'code': 'INACCURATE_INFORMATION', 'label': 'Inaccurate information'},
        {'code': 'DUPLICATE_NODE', 'label': 'Duplicate node'},
        {'code': 'UNVERIFIED_SOURCE', 'label': 'Unverified source'},
    ],
    'discussion': [
        {'code': 'OFF_TOPIC', 'label': 'Off-topic'},
        {'code': 'OFFENSIVE_LANGUAGE', 'label': 'Offensive language'},
    ],
    'profile': [
        {'code': 'FAKE_ACCOUNT', 'label': 'Fake account'},
        {'code': 'IMPERSONATION', 'label': 'Impersonation'},
    ],
}

ALLOWED_REASON_CODES = {
    'space': {item['code'] for item in (REASON_CODES['common'] + REASON_CODES['space'])},
    'node': {item['code'] for item in (REASON_CODES['common'] + REASON_CODES['node'])},
    'discussion': {item['code'] for item in (REASON_CODES['common'] + REASON_CODES['discussion'])},
    'profile': {item['code'] for item in (REASON_CODES['common'] + REASON_CODES['profile'])},
}

REASONS_VERSION = 1
