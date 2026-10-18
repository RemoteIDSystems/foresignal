//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'context_summary_activity_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ContextSummaryActivityInner {
  /// Returns a new [ContextSummaryActivityInner] instance.
  ContextSummaryActivityInner({

    required  this.at,

    required  this.ref,

    required  this.type,

    required  this.title,

     this.severity,

     this.signalFamily,
  });

  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ContextSummaryActivityInnerTypeEnum type;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'severity',
    required: false,
    includeIfNull: false,
  )


  final String? severity;



  @JsonKey(
    
    name: r'signalFamily',
    required: false,
    includeIfNull: false,
  )


  final String? signalFamily;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ContextSummaryActivityInner &&
      other.at == at &&
      other.ref == ref &&
      other.type == type &&
      other.title == title &&
      other.severity == severity &&
      other.signalFamily == signalFamily;

    @override
    int get hashCode =>
        at.hashCode +
        ref.hashCode +
        type.hashCode +
        title.hashCode +
        (severity == null ? 0 : severity.hashCode) +
        (signalFamily == null ? 0 : signalFamily.hashCode);

  factory ContextSummaryActivityInner.fromJson(Map<String, dynamic> json) => _$ContextSummaryActivityInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ContextSummaryActivityInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ContextSummaryActivityInnerTypeEnum {
@JsonValue(r'alert')
alert(r'alert'),
@JsonValue(r'finding')
finding(r'finding'),
@JsonValue(r'situation-change')
situationChange(r'situation-change'),
@JsonValue(r'record')
record(r'record');

const ContextSummaryActivityInnerTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


