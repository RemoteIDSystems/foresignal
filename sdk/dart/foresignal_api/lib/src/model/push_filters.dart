//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'push_filters.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PushFilters {
  /// Returns a new [PushFilters] instance.
  PushFilters({

     this.events,

     this.ruleIds,

     this.minSeverity,

     this.tags,
  });

  @JsonKey(
    
    name: r'events',
    required: false,
    includeIfNull: false,
  )


  final List<PushFiltersEventsEnum>? events;



      /// null = all rules
  @JsonKey(
    
    name: r'ruleIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? ruleIds;



  @JsonKey(
    
    name: r'minSeverity',
    required: false,
    includeIfNull: false,
  )


  final PushFiltersMinSeverityEnum? minSeverity;



      /// Any-of rule tags
  @JsonKey(
    
    name: r'tags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tags;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PushFilters &&
      other.events == events &&
      other.ruleIds == ruleIds &&
      other.minSeverity == minSeverity &&
      other.tags == tags;

    @override
    int get hashCode =>
        events.hashCode +
        (ruleIds == null ? 0 : ruleIds.hashCode) +
        minSeverity.hashCode +
        tags.hashCode;

  factory PushFilters.fromJson(Map<String, dynamic> json) => _$PushFiltersFromJson(json);

  Map<String, dynamic> toJson() => _$PushFiltersToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PushFiltersEventsEnum {
@JsonValue(r'alert')
alert(r'alert'),
@JsonValue(r'briefing')
briefing(r'briefing'),
@JsonValue(r'system')
system(r'system');

const PushFiltersEventsEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PushFiltersMinSeverityEnum {
@JsonValue(r'info')
info(r'info'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'critical')
critical(r'critical');

const PushFiltersMinSeverityEnum(this.value);

final String value;

@override
String toString() => value;
}


