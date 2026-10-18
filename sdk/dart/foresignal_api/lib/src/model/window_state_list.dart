//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/window_state.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'window_state_list.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WindowStateList {
  /// Returns a new [WindowStateList] instance.
  WindowStateList({

    required  this.ruleId,

    required  this.type,

    required  this.groups,

     this.groupsTruncated,
  });

  @JsonKey(
    
    name: r'ruleId',
    required: true,
    includeIfNull: false,
  )


  final String ruleId;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final WindowStateListTypeEnum type;



  @JsonKey(
    
    name: r'groups',
    required: true,
    includeIfNull: false,
  )


  final List<WindowState> groups;



  @JsonKey(
    
    name: r'groupsTruncated',
    required: false,
    includeIfNull: false,
  )


  final bool? groupsTruncated;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WindowStateList &&
      other.ruleId == ruleId &&
      other.type == type &&
      other.groups == groups &&
      other.groupsTruncated == groupsTruncated;

    @override
    int get hashCode =>
        ruleId.hashCode +
        type.hashCode +
        groups.hashCode +
        groupsTruncated.hashCode;

  factory WindowStateList.fromJson(Map<String, dynamic> json) => _$WindowStateListFromJson(json);

  Map<String, dynamic> toJson() => _$WindowStateListToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum WindowStateListTypeEnum {
@JsonValue(r'count')
count(r'count'),
@JsonValue(r'absence')
absence(r'absence'),
@JsonValue(r'anomaly')
anomaly(r'anomaly');

const WindowStateListTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


