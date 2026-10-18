//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:json_annotation/json_annotation.dart';

/// Object types that carry an owner, visibility, access list and TLP (Client-Contract §10)
enum ShareableType {
      @JsonValue(r'investigation')
      investigation(r'investigation'),
      @JsonValue(r'workspace')
      workspace(r'workspace'),
      @JsonValue(r'chronicle')
      chronicle(r'chronicle'),
      @JsonValue(r'report')
      report(r'report'),
      @JsonValue(r'saved-search')
      savedSearch(r'saved-search'),
      @JsonValue(r'annotation')
      annotation(r'annotation'),
      @JsonValue(r'attachment')
      attachment(r'attachment'),
      @JsonValue(r'evidence-item')
      evidenceItem(r'evidence-item'),
      @JsonValue(r'tag')
      tag(r'tag');

  const ShareableType(this.value);

  final String value;

  @override
  String toString() => value;
}
