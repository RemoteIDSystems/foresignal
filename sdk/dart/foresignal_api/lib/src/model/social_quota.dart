//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_quota.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialQuota {
  /// Returns a new [SocialQuota] instance.
  SocialQuota({

    required  this.unit,

     this.limit,

    required  this.window,

     this.rolling,

    required  this.used,

     this.remaining,

     this.resetsAt,

     this.description,
  });

  @JsonKey(
    
    name: r'unit',
    required: true,
    includeIfNull: false,
  )


  final SocialQuotaUnitEnum unit;



          // minimum: 0
  @JsonKey(
    
    name: r'limit',
    required: false,
    includeIfNull: false,
  )


  final int? limit;



      /// ISO-8601 duration of the window (rolling or calendar)
  @JsonKey(
    
    name: r'window',
    required: true,
    includeIfNull: false,
  )


  final String window;



  @JsonKey(
    
    name: r'rolling',
    required: false,
    includeIfNull: false,
  )


  final bool? rolling;



          // minimum: 0
  @JsonKey(
    
    name: r'used',
    required: true,
    includeIfNull: false,
  )


  final int used;



          // minimum: 0
  @JsonKey(
    
    name: r'remaining',
    required: false,
    includeIfNull: false,
  )


  final int? remaining;



  @JsonKey(
    
    name: r'resetsAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? resetsAt;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialQuota &&
      other.unit == unit &&
      other.limit == limit &&
      other.window == window &&
      other.rolling == rolling &&
      other.used == used &&
      other.remaining == remaining &&
      other.resetsAt == resetsAt &&
      other.description == description;

    @override
    int get hashCode =>
        unit.hashCode +
        (limit == null ? 0 : limit.hashCode) +
        window.hashCode +
        rolling.hashCode +
        used.hashCode +
        (remaining == null ? 0 : remaining.hashCode) +
        (resetsAt == null ? 0 : resetsAt.hashCode) +
        (description == null ? 0 : description.hashCode);

  factory SocialQuota.fromJson(Map<String, dynamic> json) => _$SocialQuotaFromJson(json);

  Map<String, dynamic> toJson() => _$SocialQuotaToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SocialQuotaUnitEnum {
@JsonValue(r'requests')
requests(r'requests'),
@JsonValue(r'units')
units(r'units'),
@JsonValue(r'queries')
queries(r'queries'),
@JsonValue(r'posts')
posts(r'posts'),
@JsonValue(r'hashtags')
hashtags(r'hashtags');

const SocialQuotaUnitEnum(this.value);

final String value;

@override
String toString() => value;
}


