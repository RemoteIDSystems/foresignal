//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/region_profile.dart';
import 'package:foresignal_api/src/model/base_rate_licences_inner.dart';
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/base_rate_definition.dart';
import 'package:foresignal_api/src/model/base_rate_unconditional.dart';
import 'package:foresignal_api/src/model/stat_interval.dart';
import 'package:foresignal_api/src/model/detector_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'base_rate.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BaseRate {
  /// Returns a new [BaseRate] instance.
  BaseRate({

    required  this.id,

     this.name,

    required  this.definition,

    required  this.referenceClass,

    required  this.ratePerWindow,

    required  this.pAtLeastOne,

    required  this.interval,

    required  this.nWindows,

    required  this.nEvents,

    required  this.periodFrom,

    required  this.periodTo,

    required  this.sourceMix,

     this.licences,

     this.unconditional,

     this.regionProfiles,

    required  this.caveats,

    required  this.computer,

    required  this.computedAt,

    required  this.origin,

     this.cached,

    required  this.manifestId,
  });

      /// Null for ad-hoc query results that were not stored
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: true,
  )


  final String? id;



  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'definition',
    required: true,
    includeIfNull: false,
  )


  final BaseRateDefinition definition;



      /// Plain-language reference class
  @JsonKey(
    
    name: r'referenceClass',
    required: true,
    includeIfNull: false,
  )


  final String referenceClass;



      /// Mean events per window
  @JsonKey(
    
    name: r'ratePerWindow',
    required: true,
    includeIfNull: false,
  )


  final num ratePerWindow;



      /// Fraction of windows with at least one event
          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'pAtLeastOne',
    required: true,
    includeIfNull: false,
  )


  final num pAtLeastOne;



  @JsonKey(
    
    name: r'interval',
    required: true,
    includeIfNull: false,
  )


  final StatInterval interval;



  @JsonKey(
    
    name: r'nWindows',
    required: true,
    includeIfNull: false,
  )


  final int nWindows;



  @JsonKey(
    
    name: r'nEvents',
    required: true,
    includeIfNull: false,
  )


  final int nEvents;



  @JsonKey(
    
    name: r'periodFrom',
    required: true,
    includeIfNull: false,
  )


  final DateTime periodFrom;



  @JsonKey(
    
    name: r'periodTo',
    required: true,
    includeIfNull: false,
  )


  final DateTime periodTo;



      /// Events per source, e.g. `{foresignal: 12, ucdp: 340}`
  @JsonKey(
    
    name: r'sourceMix',
    required: true,
    includeIfNull: false,
  )


  final Map<String, int> sourceMix;



  @JsonKey(
    
    name: r'licences',
    required: false,
    includeIfNull: false,
  )


  final List<BaseRateLicencesInner>? licences;



  @JsonKey(
    
    name: r'unconditional',
    required: false,
    includeIfNull: false,
  )


  final BaseRateUnconditional? unconditional;



  @JsonKey(
    
    name: r'regionProfiles',
    required: false,
    includeIfNull: false,
  )


  final List<RegionProfile>? regionProfiles;



      /// `SMALL_SAMPLE`, `LOW_REPORTING_DENSITY`, `MIXED_SOURCES`, `LOW_DETECTABILITY`, and `LOCAL_ONLY_WITHHELD` when the per-source counts of a LOCAL_ONLY source are not shown on the path
  @JsonKey(
    
    name: r'caveats',
    required: true,
    includeIfNull: false,
  )


  final List<Caveat> caveats;



  @JsonKey(
    
    name: r'computer',
    required: true,
    includeIfNull: false,
  )


  final DetectorRef computer;



  @JsonKey(
    
    name: r'computedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime computedAt;



  @JsonKey(
    
    name: r'origin',
    required: true,
    includeIfNull: false,
  )


  final BaseRateOriginEnum origin;



      /// Ad-hoc queries: served from the 24 h cache
  @JsonKey(
    
    name: r'cached',
    required: false,
    includeIfNull: false,
  )


  final bool? cached;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: true,
    includeIfNull: false,
  )


  final String manifestId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BaseRate &&
      other.id == id &&
      other.name == name &&
      other.definition == definition &&
      other.referenceClass == referenceClass &&
      other.ratePerWindow == ratePerWindow &&
      other.pAtLeastOne == pAtLeastOne &&
      other.interval == interval &&
      other.nWindows == nWindows &&
      other.nEvents == nEvents &&
      other.periodFrom == periodFrom &&
      other.periodTo == periodTo &&
      other.sourceMix == sourceMix &&
      other.licences == licences &&
      other.unconditional == unconditional &&
      other.regionProfiles == regionProfiles &&
      other.caveats == caveats &&
      other.computer == computer &&
      other.computedAt == computedAt &&
      other.origin == origin &&
      other.cached == cached &&
      other.manifestId == manifestId;

    @override
    int get hashCode =>
        (id == null ? 0 : id.hashCode) +
        (name == null ? 0 : name.hashCode) +
        definition.hashCode +
        referenceClass.hashCode +
        ratePerWindow.hashCode +
        pAtLeastOne.hashCode +
        interval.hashCode +
        nWindows.hashCode +
        nEvents.hashCode +
        periodFrom.hashCode +
        periodTo.hashCode +
        sourceMix.hashCode +
        licences.hashCode +
        (unconditional == null ? 0 : unconditional.hashCode) +
        regionProfiles.hashCode +
        caveats.hashCode +
        computer.hashCode +
        computedAt.hashCode +
        origin.hashCode +
        cached.hashCode +
        manifestId.hashCode;

  factory BaseRate.fromJson(Map<String, dynamic> json) => _$BaseRateFromJson(json);

  Map<String, dynamic> toJson() => _$BaseRateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum BaseRateOriginEnum {
@JsonValue(r'auto')
auto(r'auto'),
@JsonValue(r'operator')
operator_(r'operator'),
@JsonValue(r'agent')
agent(r'agent');

const BaseRateOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


