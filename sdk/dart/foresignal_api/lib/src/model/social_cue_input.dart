//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/social_platform_id.dart';
import 'package:foresignal_api/src/model/time_range.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_cue_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialCueInput {
  /// Returns a new [SocialCueInput] instance.
  SocialCueInput({

     this.placeId,

     this.regionCode,

     this.bbox,

     this.entityRef,

     this.situationId,

     this.terms,

     this.families,

     this.platforms,

    required  this.window,

     this.maxCostUsd,
  });

      /// 036 place
  @JsonKey(
    
    name: r'placeId',
    required: false,
    includeIfNull: false,
  )


  final String? placeId;



      /// 040 region
  @JsonKey(
    
    name: r'regionCode',
    required: false,
    includeIfNull: false,
  )


  final String? regionCode;



      /// minLon,minLat,maxLon,maxLat
  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final String? bbox;



      /// 098 entity
  @JsonKey(
    
    name: r'entityRef',
    required: false,
    includeIfNull: false,
  )


  final String? entityRef;



      /// ULID identifier
  @JsonKey(
    
    name: r'situationId',
    required: false,
    includeIfNull: false,
  )


  final String? situationId;



      /// Extra terms added to the generated queries
  @JsonKey(
    
    name: r'terms',
    required: false,
    includeIfNull: false,
  )


  final List<String>? terms;



      /// Signal families whose lexicon terms are added (default from the situation or none)
  @JsonKey(
    
    name: r'families',
    required: false,
    includeIfNull: false,
  )


  final List<String>? families;



      /// Search-capable adapters; default all enabled ones
  @JsonKey(
    
    name: r'platforms',
    required: false,
    includeIfNull: false,
  )


  final List<SocialPlatformId>? platforms;



  @JsonKey(
    
    name: r'window',
    required: true,
    includeIfNull: false,
  )


  final TimeRange window;



      /// Cost cap; default and ceiling `social.cues.maxCostUsdPerCue` (1.00)
          // minimum: 0
  @JsonKey(
    
    name: r'maxCostUsd',
    required: false,
    includeIfNull: false,
  )


  final num? maxCostUsd;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialCueInput &&
      other.placeId == placeId &&
      other.regionCode == regionCode &&
      other.bbox == bbox &&
      other.entityRef == entityRef &&
      other.situationId == situationId &&
      other.terms == terms &&
      other.families == families &&
      other.platforms == platforms &&
      other.window == window &&
      other.maxCostUsd == maxCostUsd;

    @override
    int get hashCode =>
        (placeId == null ? 0 : placeId.hashCode) +
        (regionCode == null ? 0 : regionCode.hashCode) +
        (bbox == null ? 0 : bbox.hashCode) +
        (entityRef == null ? 0 : entityRef.hashCode) +
        (situationId == null ? 0 : situationId.hashCode) +
        terms.hashCode +
        families.hashCode +
        platforms.hashCode +
        window.hashCode +
        (maxCostUsd == null ? 0 : maxCostUsd.hashCode);

  factory SocialCueInput.fromJson(Map<String, dynamic> json) => _$SocialCueInputFromJson(json);

  Map<String, dynamic> toJson() => _$SocialCueInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

