//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_platform_price.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialPlatformPrice {
  /// Returns a new [SocialPlatformPrice] instance.
  SocialPlatformPrice({

    required  this.item,

    required  this.usd,

     this.per = 1,
  });

      /// Billable item, e.g. `counts-request`, `post-read`, `search-request`
  @JsonKey(
    
    name: r'item',
    required: true,
    includeIfNull: false,
  )


  final String item;



      /// US dollars
          // minimum: 0
  @JsonKey(
    
    name: r'usd',
    required: true,
    includeIfNull: false,
  )


  final num usd;



      /// Units the price covers
          // minimum: 1
  @JsonKey(
    defaultValue: 1,
    name: r'per',
    required: false,
    includeIfNull: false,
  )


  final int? per;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialPlatformPrice &&
      other.item == item &&
      other.usd == usd &&
      other.per == per;

    @override
    int get hashCode =>
        item.hashCode +
        usd.hashCode +
        per.hashCode;

  factory SocialPlatformPrice.fromJson(Map<String, dynamic> json) => _$SocialPlatformPriceFromJson(json);

  Map<String, dynamic> toJson() => _$SocialPlatformPriceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

