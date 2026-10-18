// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'coverage_stats.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CoverageStatsCWProxy {
  CoverageStats scope(CoverageStatsScopeEnum? scope);

  CoverageStats articles(CoverageStatsArticles articles);

  CoverageStats evidenceGroups(CoverageStatsArticles evidenceGroups);

  CoverageStats byCountry(Map<String, int>? byCountry);

  CoverageStats byOutletType(Map<String, int>? byOutletType);

  CoverageStats byFactuality(Map<String, int>? byFactuality);

  CoverageStats coverageBalance(CoverageStatsCoverageBalance coverageBalance);

  CoverageStats stateAffiliatedShare(num stateAffiliatedShare);

  CoverageStats nRated(int nRated);

  CoverageStats blindspot(CoverageStatsBlindspotEnum? blindspot);

  CoverageStats corroborated(bool corroborated);

  CoverageStats corroboratedShare(num? corroboratedShare);

  CoverageStats skewed(bool? skewed);

  CoverageStats caveats(List<Caveat>? caveats);

  CoverageStats analyzer(DetectorRef? analyzer);

  CoverageStats manifestId(String? manifestId);

  CoverageStats computedAt(DateTime? computedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CoverageStats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CoverageStats(...).copyWith(id: 12, name: "My name")
  /// ```
  CoverageStats call({
    CoverageStatsScopeEnum? scope,
    CoverageStatsArticles articles,
    CoverageStatsArticles evidenceGroups,
    Map<String, int>? byCountry,
    Map<String, int>? byOutletType,
    Map<String, int>? byFactuality,
    CoverageStatsCoverageBalance coverageBalance,
    num stateAffiliatedShare,
    int nRated,
    CoverageStatsBlindspotEnum? blindspot,
    bool corroborated,
    num? corroboratedShare,
    bool? skewed,
    List<Caveat>? caveats,
    DetectorRef? analyzer,
    String? manifestId,
    DateTime? computedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCoverageStats.copyWith(...)` or call `instanceOfCoverageStats.copyWith.fieldName(value)` for a single field.
class _$CoverageStatsCWProxyImpl implements _$CoverageStatsCWProxy {
  const _$CoverageStatsCWProxyImpl(this._value);

  final CoverageStats _value;

  @override
  CoverageStats scope(CoverageStatsScopeEnum? scope) => call(scope: scope);

  @override
  CoverageStats articles(CoverageStatsArticles articles) =>
      call(articles: articles);

  @override
  CoverageStats evidenceGroups(CoverageStatsArticles evidenceGroups) =>
      call(evidenceGroups: evidenceGroups);

  @override
  CoverageStats byCountry(Map<String, int>? byCountry) =>
      call(byCountry: byCountry);

  @override
  CoverageStats byOutletType(Map<String, int>? byOutletType) =>
      call(byOutletType: byOutletType);

  @override
  CoverageStats byFactuality(Map<String, int>? byFactuality) =>
      call(byFactuality: byFactuality);

  @override
  CoverageStats coverageBalance(CoverageStatsCoverageBalance coverageBalance) =>
      call(coverageBalance: coverageBalance);

  @override
  CoverageStats stateAffiliatedShare(num stateAffiliatedShare) =>
      call(stateAffiliatedShare: stateAffiliatedShare);

  @override
  CoverageStats nRated(int nRated) => call(nRated: nRated);

  @override
  CoverageStats blindspot(CoverageStatsBlindspotEnum? blindspot) =>
      call(blindspot: blindspot);

  @override
  CoverageStats corroborated(bool corroborated) =>
      call(corroborated: corroborated);

  @override
  CoverageStats corroboratedShare(num? corroboratedShare) =>
      call(corroboratedShare: corroboratedShare);

  @override
  CoverageStats skewed(bool? skewed) => call(skewed: skewed);

  @override
  CoverageStats caveats(List<Caveat>? caveats) => call(caveats: caveats);

  @override
  CoverageStats analyzer(DetectorRef? analyzer) => call(analyzer: analyzer);

  @override
  CoverageStats manifestId(String? manifestId) => call(manifestId: manifestId);

  @override
  CoverageStats computedAt(DateTime? computedAt) =>
      call(computedAt: computedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CoverageStats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CoverageStats(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CoverageStats call({
    Object? scope = const $CopyWithPlaceholder(),
    Object? articles = const $CopyWithPlaceholder(),
    Object? evidenceGroups = const $CopyWithPlaceholder(),
    Object? byCountry = const $CopyWithPlaceholder(),
    Object? byOutletType = const $CopyWithPlaceholder(),
    Object? byFactuality = const $CopyWithPlaceholder(),
    Object? coverageBalance = const $CopyWithPlaceholder(),
    Object? stateAffiliatedShare = const $CopyWithPlaceholder(),
    Object? nRated = const $CopyWithPlaceholder(),
    Object? blindspot = const $CopyWithPlaceholder(),
    Object? corroborated = const $CopyWithPlaceholder(),
    Object? corroboratedShare = const $CopyWithPlaceholder(),
    Object? skewed = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
    Object? analyzer = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
    Object? computedAt = const $CopyWithPlaceholder(),
  }) {
    return CoverageStats(
      scope: scope == const $CopyWithPlaceholder()
          ? _value.scope
          // ignore: cast_nullable_to_non_nullable
          : scope as CoverageStatsScopeEnum?,
      articles: articles == const $CopyWithPlaceholder() || articles == null
          ? _value.articles
          // ignore: cast_nullable_to_non_nullable
          : articles as CoverageStatsArticles,
      evidenceGroups:
          evidenceGroups == const $CopyWithPlaceholder() ||
              evidenceGroups == null
          ? _value.evidenceGroups
          // ignore: cast_nullable_to_non_nullable
          : evidenceGroups as CoverageStatsArticles,
      byCountry: byCountry == const $CopyWithPlaceholder()
          ? _value.byCountry
          // ignore: cast_nullable_to_non_nullable
          : byCountry as Map<String, int>?,
      byOutletType: byOutletType == const $CopyWithPlaceholder()
          ? _value.byOutletType
          // ignore: cast_nullable_to_non_nullable
          : byOutletType as Map<String, int>?,
      byFactuality: byFactuality == const $CopyWithPlaceholder()
          ? _value.byFactuality
          // ignore: cast_nullable_to_non_nullable
          : byFactuality as Map<String, int>?,
      coverageBalance:
          coverageBalance == const $CopyWithPlaceholder() ||
              coverageBalance == null
          ? _value.coverageBalance
          // ignore: cast_nullable_to_non_nullable
          : coverageBalance as CoverageStatsCoverageBalance,
      stateAffiliatedShare:
          stateAffiliatedShare == const $CopyWithPlaceholder() ||
              stateAffiliatedShare == null
          ? _value.stateAffiliatedShare
          // ignore: cast_nullable_to_non_nullable
          : stateAffiliatedShare as num,
      nRated: nRated == const $CopyWithPlaceholder() || nRated == null
          ? _value.nRated
          // ignore: cast_nullable_to_non_nullable
          : nRated as int,
      blindspot: blindspot == const $CopyWithPlaceholder()
          ? _value.blindspot
          // ignore: cast_nullable_to_non_nullable
          : blindspot as CoverageStatsBlindspotEnum?,
      corroborated:
          corroborated == const $CopyWithPlaceholder() || corroborated == null
          ? _value.corroborated
          // ignore: cast_nullable_to_non_nullable
          : corroborated as bool,
      corroboratedShare: corroboratedShare == const $CopyWithPlaceholder()
          ? _value.corroboratedShare
          // ignore: cast_nullable_to_non_nullable
          : corroboratedShare as num?,
      skewed: skewed == const $CopyWithPlaceholder()
          ? _value.skewed
          // ignore: cast_nullable_to_non_nullable
          : skewed as bool?,
      caveats: caveats == const $CopyWithPlaceholder()
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>?,
      analyzer: analyzer == const $CopyWithPlaceholder()
          ? _value.analyzer
          // ignore: cast_nullable_to_non_nullable
          : analyzer as DetectorRef?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
      computedAt: computedAt == const $CopyWithPlaceholder()
          ? _value.computedAt
          // ignore: cast_nullable_to_non_nullable
          : computedAt as DateTime?,
    );
  }
}

extension $CoverageStatsCopyWith on CoverageStats {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCoverageStats.copyWith(...)` or `instanceOfCoverageStats.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CoverageStatsCWProxy get copyWith => _$CoverageStatsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CoverageStats _$CoverageStatsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CoverageStats', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'articles',
      'evidenceGroups',
      'coverageBalance',
      'stateAffiliatedShare',
      'nRated',
      'corroborated',
    ],
  );
  final val = CoverageStats(
    scope: $checkedConvert(
      'scope',
      (v) => $enumDecodeNullable(_$CoverageStatsScopeEnumEnumMap, v),
    ),
    articles: $checkedConvert(
      'articles',
      (v) => CoverageStatsArticles.fromJson(v as Map<String, dynamic>),
    ),
    evidenceGroups: $checkedConvert(
      'evidenceGroups',
      (v) => CoverageStatsArticles.fromJson(v as Map<String, dynamic>),
    ),
    byCountry: $checkedConvert(
      'byCountry',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(k, (e as num).toInt()),
      ),
    ),
    byOutletType: $checkedConvert(
      'byOutletType',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(k, (e as num).toInt()),
      ),
    ),
    byFactuality: $checkedConvert(
      'byFactuality',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(k, (e as num).toInt()),
      ),
    ),
    coverageBalance: $checkedConvert(
      'coverageBalance',
      (v) => CoverageStatsCoverageBalance.fromJson(v as Map<String, dynamic>),
    ),
    stateAffiliatedShare: $checkedConvert(
      'stateAffiliatedShare',
      (v) => v as num,
    ),
    nRated: $checkedConvert('nRated', (v) => (v as num).toInt()),
    blindspot: $checkedConvert(
      'blindspot',
      (v) => $enumDecodeNullable(_$CoverageStatsBlindspotEnumEnumMap, v),
    ),
    corroborated: $checkedConvert('corroborated', (v) => v as bool),
    corroboratedShare: $checkedConvert('corroboratedShare', (v) => v as num?),
    skewed: $checkedConvert('skewed', (v) => v as bool?),
    caveats: $checkedConvert(
      'caveats',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Caveat.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    analyzer: $checkedConvert(
      'analyzer',
      (v) => v == null ? null : DetectorRef.fromJson(v as Map<String, dynamic>),
    ),
    manifestId: $checkedConvert('manifestId', (v) => v as String?),
    computedAt: $checkedConvert(
      'computedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$CoverageStatsToJson(CoverageStats instance) =>
    <String, dynamic>{
      'scope': ?_$CoverageStatsScopeEnumEnumMap[instance.scope],
      'articles': instance.articles.toJson(),
      'evidenceGroups': instance.evidenceGroups.toJson(),
      'byCountry': ?instance.byCountry,
      'byOutletType': ?instance.byOutletType,
      'byFactuality': ?instance.byFactuality,
      'coverageBalance': instance.coverageBalance.toJson(),
      'stateAffiliatedShare': instance.stateAffiliatedShare,
      'nRated': instance.nRated,
      'blindspot': ?_$CoverageStatsBlindspotEnumEnumMap[instance.blindspot],
      'corroborated': instance.corroborated,
      'corroboratedShare': ?instance.corroboratedShare,
      'skewed': ?instance.skewed,
      'caveats': ?instance.caveats?.map((e) => e.toJson()).toList(),
      'analyzer': ?instance.analyzer?.toJson(),
      'manifestId': ?instance.manifestId,
      'computedAt': ?instance.computedAt?.toIso8601String(),
    };

const _$CoverageStatsScopeEnumEnumMap = {
  CoverageStatsScopeEnum.story: 'story',
  CoverageStatsScopeEnum.topic: 'topic',
  CoverageStatsScopeEnum.situation: 'situation',
  CoverageStatsScopeEnum.hypothesis: 'hypothesis',
};

const _$CoverageStatsBlindspotEnumEnumMap = {
  CoverageStatsBlindspotEnum.left: 'left',
  CoverageStatsBlindspotEnum.right: 'right',
};
