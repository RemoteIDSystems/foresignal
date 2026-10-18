//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/detector_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'base_rate_refresh_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BaseRateRefreshResult {
  /// Returns a new [BaseRateRefreshResult] instance.
  BaseRateRefreshResult({

    required  this.profiles,

    required  this.rates,

     this.skipped,

     this.removed,

    required  this.families,

     this.regions,

     this.computer,
  });

      /// Region profiles written
  @JsonKey(
    
    name: r'profiles',
    required: true,
    includeIfNull: false,
  )


  final int profiles;



      /// Automatic rates written (family × region × window)
  @JsonKey(
    
    name: r'rates',
    required: true,
    includeIfNull: false,
  )


  final int rates;



      /// Family-region pairs with under 52 covered weeks
  @JsonKey(
    
    name: r'skipped',
    required: false,
    includeIfNull: false,
  )


  final int? skipped;



      /// Automatic rates no longer produced and removed
  @JsonKey(
    
    name: r'removed',
    required: false,
    includeIfNull: false,
  )


  final int? removed;



  @JsonKey(
    
    name: r'families',
    required: true,
    includeIfNull: false,
  )


  final List<String> families;



      /// Regions with at least one automatic rate
  @JsonKey(
    
    name: r'regions',
    required: false,
    includeIfNull: false,
  )


  final int? regions;



  @JsonKey(
    
    name: r'computer',
    required: false,
    includeIfNull: false,
  )


  final DetectorRef? computer;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BaseRateRefreshResult &&
      other.profiles == profiles &&
      other.rates == rates &&
      other.skipped == skipped &&
      other.removed == removed &&
      other.families == families &&
      other.regions == regions &&
      other.computer == computer;

    @override
    int get hashCode =>
        profiles.hashCode +
        rates.hashCode +
        skipped.hashCode +
        removed.hashCode +
        families.hashCode +
        regions.hashCode +
        computer.hashCode;

  factory BaseRateRefreshResult.fromJson(Map<String, dynamic> json) => _$BaseRateRefreshResultFromJson(json);

  Map<String, dynamic> toJson() => _$BaseRateRefreshResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

