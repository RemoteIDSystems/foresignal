//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_lead_times.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialLeadTimes {
  /// Returns a new [SocialLeadTimes] instance.
  SocialLeadTimes({

    required  this.socialFirst,

     this.physicalFirst,

     this.newsFirst,

     this.officialFirst,

     this.leadOverPhysicalSeconds,

     this.leadOverNewsSeconds,

     this.leadOverOfficialSeconds,
  });

  @JsonKey(
    
    name: r'socialFirst',
    required: true,
    includeIfNull: false,
  )


  final DateTime socialFirst;



  @JsonKey(
    
    name: r'physicalFirst',
    required: false,
    includeIfNull: false,
  )


  final DateTime? physicalFirst;



  @JsonKey(
    
    name: r'newsFirst',
    required: false,
    includeIfNull: false,
  )


  final DateTime? newsFirst;



  @JsonKey(
    
    name: r'officialFirst',
    required: false,
    includeIfNull: false,
  )


  final DateTime? officialFirst;



  @JsonKey(
    
    name: r'leadOverPhysicalSeconds',
    required: false,
    includeIfNull: false,
  )


  final num? leadOverPhysicalSeconds;



  @JsonKey(
    
    name: r'leadOverNewsSeconds',
    required: false,
    includeIfNull: false,
  )


  final num? leadOverNewsSeconds;



  @JsonKey(
    
    name: r'leadOverOfficialSeconds',
    required: false,
    includeIfNull: false,
  )


  final num? leadOverOfficialSeconds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialLeadTimes &&
      other.socialFirst == socialFirst &&
      other.physicalFirst == physicalFirst &&
      other.newsFirst == newsFirst &&
      other.officialFirst == officialFirst &&
      other.leadOverPhysicalSeconds == leadOverPhysicalSeconds &&
      other.leadOverNewsSeconds == leadOverNewsSeconds &&
      other.leadOverOfficialSeconds == leadOverOfficialSeconds;

    @override
    int get hashCode =>
        socialFirst.hashCode +
        (physicalFirst == null ? 0 : physicalFirst.hashCode) +
        (newsFirst == null ? 0 : newsFirst.hashCode) +
        (officialFirst == null ? 0 : officialFirst.hashCode) +
        (leadOverPhysicalSeconds == null ? 0 : leadOverPhysicalSeconds.hashCode) +
        (leadOverNewsSeconds == null ? 0 : leadOverNewsSeconds.hashCode) +
        (leadOverOfficialSeconds == null ? 0 : leadOverOfficialSeconds.hashCode);

  factory SocialLeadTimes.fromJson(Map<String, dynamic> json) => _$SocialLeadTimesFromJson(json);

  Map<String, dynamic> toJson() => _$SocialLeadTimesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

