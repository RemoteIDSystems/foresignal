//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:foresignal_api/src/model/shared_content_links.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'shared_content.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SharedContent {
  /// Returns a new [SharedContent] instance.
  SharedContent({

    required  this.targetType,

     this.title,

    required  this.expiresAt,

    required  this.masked,

     this.precisionM,

    required  this.data,

    required  this.attributions,

     this.links,
  });

  @JsonKey(
    
    name: r'targetType',
    required: true,
    includeIfNull: false,
  )


  final SharedContentTargetTypeEnum targetType;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'expiresAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime expiresAt;



  @JsonKey(
    
    name: r'masked',
    required: true,
    includeIfNull: false,
  )


  final bool masked;



  @JsonKey(
    
    name: r'precisionM',
    required: false,
    includeIfNull: false,
  )


  final int? precisionM;



      /// The target's JSON after licence filtering, privacy masking and the field allowlist
  @JsonKey(
    
    name: r'data',
    required: true,
    includeIfNull: false,
  )


  final Map<String, Object> data;



  @JsonKey(
    
    name: r'attributions',
    required: true,
    includeIfNull: false,
  )


  final List<Attribution> attributions;



  @JsonKey(
    
    name: r'links',
    required: false,
    includeIfNull: false,
  )


  final SharedContentLinks? links;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SharedContent &&
      other.targetType == targetType &&
      other.title == title &&
      other.expiresAt == expiresAt &&
      other.masked == masked &&
      other.precisionM == precisionM &&
      other.data == data &&
      other.attributions == attributions &&
      other.links == links;

    @override
    int get hashCode =>
        targetType.hashCode +
        (title == null ? 0 : title.hashCode) +
        expiresAt.hashCode +
        masked.hashCode +
        (precisionM == null ? 0 : precisionM.hashCode) +
        data.hashCode +
        attributions.hashCode +
        links.hashCode;

  factory SharedContent.fromJson(Map<String, dynamic> json) => _$SharedContentFromJson(json);

  Map<String, dynamic> toJson() => _$SharedContentToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SharedContentTargetTypeEnum {
@JsonValue(r'record')
record(r'record'),
@JsonValue(r'alert')
alert(r'alert'),
@JsonValue(r'view')
view(r'view'),
@JsonValue(r'track')
track(r'track'),
@JsonValue(r'briefing-run')
briefingRun(r'briefing-run'),
@JsonValue(r'export')
export_(r'export');

const SharedContentTargetTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


