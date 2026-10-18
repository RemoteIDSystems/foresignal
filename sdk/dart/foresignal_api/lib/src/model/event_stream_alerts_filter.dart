//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'event_stream_alerts_filter.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EventStreamAlertsFilter {
  /// Returns a new [EventStreamAlertsFilter] instance.
  EventStreamAlertsFilter({

     this.minSeverity,

     this.ruleId,

     this.source_,

     this.kind,

     this.includeSuppressed = false,
  });

  @JsonKey(
    
    name: r'minSeverity',
    required: false,
    includeIfNull: false,
  )


  final EventStreamAlertsFilterMinSeverityEnum? minSeverity;



      /// Rule ids
  @JsonKey(
    
    name: r'ruleId',
    required: false,
    includeIfNull: false,
  )


  final List<String>? ruleId;



      /// Source ids
  @JsonKey(
    
    name: r'source',
    required: false,
    includeIfNull: false,
  )


  final List<String>? source_;



      /// Kinds
  @JsonKey(
    
    name: r'kind',
    required: false,
    includeIfNull: false,
  )


  final List<String>? kind;



      /// Include alerts suppressed by a rule or alert snooze
  @JsonKey(
    defaultValue: false,
    name: r'includeSuppressed',
    required: false,
    includeIfNull: false,
  )


  final bool? includeSuppressed;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EventStreamAlertsFilter &&
      other.minSeverity == minSeverity &&
      other.ruleId == ruleId &&
      other.source_ == source_ &&
      other.kind == kind &&
      other.includeSuppressed == includeSuppressed;

    @override
    int get hashCode =>
        (minSeverity == null ? 0 : minSeverity.hashCode) +
        ruleId.hashCode +
        source_.hashCode +
        kind.hashCode +
        includeSuppressed.hashCode;

  factory EventStreamAlertsFilter.fromJson(Map<String, dynamic> json) => _$EventStreamAlertsFilterFromJson(json);

  Map<String, dynamic> toJson() => _$EventStreamAlertsFilterToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EventStreamAlertsFilterMinSeverityEnum {
@JsonValue(r'info')
info(r'info'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'critical')
critical(r'critical');

const EventStreamAlertsFilterMinSeverityEnum(this.value);

final String value;

@override
String toString() => value;
}


