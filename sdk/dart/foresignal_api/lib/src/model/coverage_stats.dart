//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/coverage_stats_coverage_balance.dart';
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/coverage_stats_articles.dart';
import 'package:foresignal_api/src/model/detector_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'coverage_stats.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CoverageStats {
  /// Returns a new [CoverageStats] instance.
  CoverageStats({

     this.scope,

    required  this.articles,

    required  this.evidenceGroups,

     this.byCountry,

     this.byOutletType,

     this.byFactuality,

    required  this.coverageBalance,

    required  this.stateAffiliatedShare,

    required  this.nRated,

     this.blindspot,

    required  this.corroborated,

     this.corroboratedShare,

     this.skewed,

     this.caveats,

     this.analyzer,

     this.manifestId,

     this.computedAt,
  });

  @JsonKey(
    
    name: r'scope',
    required: false,
    includeIfNull: false,
  )


  final CoverageStatsScopeEnum? scope;



  @JsonKey(
    
    name: r'articles',
    required: true,
    includeIfNull: false,
  )


  final CoverageStatsArticles articles;



  @JsonKey(
    
    name: r'evidenceGroups',
    required: true,
    includeIfNull: false,
  )


  final CoverageStatsArticles evidenceGroups;



      /// ISO country → evidence groups
  @JsonKey(
    
    name: r'byCountry',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? byCountry;



      /// Outlet type → evidence groups
  @JsonKey(
    
    name: r'byOutletType',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? byOutletType;



      /// Factuality band (1–5, unrated) → evidence groups
  @JsonKey(
    
    name: r'byFactuality',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? byFactuality;



  @JsonKey(
    
    name: r'coverageBalance',
    required: true,
    includeIfNull: false,
  )


  final CoverageStatsCoverageBalance coverageBalance;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'stateAffiliatedShare',
    required: true,
    includeIfNull: false,
  )


  final num stateAffiliatedShare;



      /// Rated evidence groups
  @JsonKey(
    
    name: r'nRated',
    required: true,
    includeIfNull: false,
  )


  final int nRated;



      /// ≥ 75% of rated evidence groups from one side with nRated ≥ 8
  @JsonKey(
    
    name: r'blindspot',
    required: false,
    includeIfNull: false,
  )


  final CoverageStatsBlindspotEnum? blindspot;



      /// Linked to ≥ 1 non-news evidence group in the same place and window (116)
  @JsonKey(
    
    name: r'corroborated',
    required: true,
    includeIfNull: false,
  )


  final bool corroborated;



      /// Situations: share of news members corroborated
          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'corroboratedShare',
    required: false,
    includeIfNull: false,
  )


  final num? corroboratedShare;



      /// ≥ 75% from one bucket or from state-affiliated outlets (SKEWED_COVERAGE)
  @JsonKey(
    
    name: r'skewed',
    required: false,
    includeIfNull: false,
  )


  final bool? skewed;



  @JsonKey(
    
    name: r'caveats',
    required: false,
    includeIfNull: false,
  )


  final List<Caveat>? caveats;



  @JsonKey(
    
    name: r'analyzer',
    required: false,
    includeIfNull: false,
  )


  final DetectorRef? analyzer;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;



  @JsonKey(
    
    name: r'computedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? computedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CoverageStats &&
      other.scope == scope &&
      other.articles == articles &&
      other.evidenceGroups == evidenceGroups &&
      other.byCountry == byCountry &&
      other.byOutletType == byOutletType &&
      other.byFactuality == byFactuality &&
      other.coverageBalance == coverageBalance &&
      other.stateAffiliatedShare == stateAffiliatedShare &&
      other.nRated == nRated &&
      other.blindspot == blindspot &&
      other.corroborated == corroborated &&
      other.corroboratedShare == corroboratedShare &&
      other.skewed == skewed &&
      other.caveats == caveats &&
      other.analyzer == analyzer &&
      other.manifestId == manifestId &&
      other.computedAt == computedAt;

    @override
    int get hashCode =>
        scope.hashCode +
        articles.hashCode +
        evidenceGroups.hashCode +
        byCountry.hashCode +
        byOutletType.hashCode +
        byFactuality.hashCode +
        coverageBalance.hashCode +
        stateAffiliatedShare.hashCode +
        nRated.hashCode +
        (blindspot == null ? 0 : blindspot.hashCode) +
        corroborated.hashCode +
        (corroboratedShare == null ? 0 : corroboratedShare.hashCode) +
        skewed.hashCode +
        caveats.hashCode +
        analyzer.hashCode +
        (manifestId == null ? 0 : manifestId.hashCode) +
        computedAt.hashCode;

  factory CoverageStats.fromJson(Map<String, dynamic> json) => _$CoverageStatsFromJson(json);

  Map<String, dynamic> toJson() => _$CoverageStatsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum CoverageStatsScopeEnum {
@JsonValue(r'story')
story(r'story'),
@JsonValue(r'topic')
topic(r'topic'),
@JsonValue(r'situation')
situation(r'situation'),
@JsonValue(r'hypothesis')
hypothesis(r'hypothesis');

const CoverageStatsScopeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// ≥ 75% of rated evidence groups from one side with nRated ≥ 8
enum CoverageStatsBlindspotEnum {
@JsonValue(r'left')
left(r'left'),
@JsonValue(r'right')
right(r'right');

const CoverageStatsBlindspotEnum(this.value);

final String value;

@override
String toString() => value;
}


