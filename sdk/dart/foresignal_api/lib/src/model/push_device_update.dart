//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/push_filters.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'push_device_update.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PushDeviceUpdate {
  /// Returns a new [PushDeviceUpdate] instance.
  PushDeviceUpdate({

     this.name,

     this.filters,

     this.contentLevel,

     this.lang,
  });

  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'filters',
    required: false,
    includeIfNull: false,
  )


  final PushFilters? filters;



  @JsonKey(
    
    name: r'contentLevel',
    required: false,
    includeIfNull: false,
  )


  final PushDeviceUpdateContentLevelEnum? contentLevel;



  @JsonKey(
    
    name: r'lang',
    required: false,
    includeIfNull: false,
  )


  final String? lang;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PushDeviceUpdate &&
      other.name == name &&
      other.filters == filters &&
      other.contentLevel == contentLevel &&
      other.lang == lang;

    @override
    int get hashCode =>
        (name == null ? 0 : name.hashCode) +
        filters.hashCode +
        contentLevel.hashCode +
        (lang == null ? 0 : lang.hashCode);

  factory PushDeviceUpdate.fromJson(Map<String, dynamic> json) => _$PushDeviceUpdateFromJson(json);

  Map<String, dynamic> toJson() => _$PushDeviceUpdateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PushDeviceUpdateContentLevelEnum {
@JsonValue(r'minimal')
minimal(r'minimal'),
@JsonValue(r'short')
short(r'short');

const PushDeviceUpdateContentLevelEnum(this.value);

final String value;

@override
String toString() => value;
}


