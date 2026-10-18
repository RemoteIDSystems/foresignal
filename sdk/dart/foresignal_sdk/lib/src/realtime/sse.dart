/// An incremental Server-Sent Events parser (WHATWG HTML "event stream interpretation").
library;

/// One dispatched event.
class SseMessage {
  /// Creates the message.
  const SseMessage(this.event, this.data, this.id);

  /// The `event:` name (`message` when absent).
  final String event;

  /// The `data:` lines joined with `\n`.
  final String data;

  /// The `id:` of this event, only when the event carried one.
  final String? id;
}

/// Parses an event stream fed with decoded text chunks of any size.
class SseParser {
  String _buffer = '';
  bool _started = false;
  String _event = '';
  final List<String> _data = [];
  String? _id;
  bool _hasId = false;

  /// The latest `retry:` value in milliseconds, or null.
  int? retryMs;

  /// Feeds a chunk of text and returns the events it completed.
  List<SseMessage> feed(String chunk) {
    var text = _buffer + chunk;
    if (!_started && text.isNotEmpty) {
      _started = true;
      if (text.codeUnitAt(0) == 0xFEFF) {
        text = text.substring(1);
      }
    }
    final out = <SseMessage>[];
    var pos = 0;
    while (true) {
      var end = -1;
      for (var i = pos; i < text.length; i++) {
        final c = text.codeUnitAt(i);
        if (c == 10 || c == 13) {
          end = i;
          break;
        }
      }
      if (end < 0) {
        break;
      }
      if (text.codeUnitAt(end) == 13 && end == text.length - 1) {
        break; // possibly the first half of CRLF
      }
      final line = text.substring(pos, end);
      pos = end + (text.codeUnitAt(end) == 13 && text.codeUnitAt(end + 1) == 10 ? 2 : 1);
      _line(line, out);
    }
    _buffer = text.substring(pos);
    return out;
  }

  void _line(String line, List<SseMessage> out) {
    if (line.isEmpty) {
      _dispatch(out);
      return;
    }
    if (line.startsWith(':')) {
      return;
    }
    final colon = line.indexOf(':');
    final field = colon < 0 ? line : line.substring(0, colon);
    var value = colon < 0 ? '' : line.substring(colon + 1);
    if (value.startsWith(' ')) {
      value = value.substring(1);
    }
    switch (field) {
      case 'event':
        _event = value;
      case 'data':
        _data.add(value);
      case 'id':
        if (!value.contains('\u0000')) {
          _id = value;
          _hasId = true;
        }
      case 'retry':
        if (RegExp(r'^\d+$').hasMatch(value)) {
          retryMs = int.parse(value);
        }
    }
  }

  void _dispatch(List<SseMessage> out) {
    if (_data.isNotEmpty) {
      out.add(SseMessage(_event.isEmpty ? 'message' : _event, _data.join('\n'), _hasId && _id != '' ? _id : null));
    }
    _event = '';
    _data.clear();
    _id = null;
    _hasId = false;
  }
}
