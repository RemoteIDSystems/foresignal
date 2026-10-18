// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'regional_outlook.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RegionalOutlookCWProxy {
  RegionalOutlook region(String region);

  RegionalOutlook regionLevel(RegionalOutlookRegionLevelEnum regionLevel);

  RegionalOutlook family(String family);

  RegionalOutlook category(String? category);

  RegionalOutlook publishedAt(DateTime publishedAt);

  RegionalOutlook modelVersion(String modelVersion);

  RegionalOutlook asOfSeq(int? asOfSeq);

  RegionalOutlook manifestId(String? manifestId);

  RegionalOutlook windows(List<OutlookWindow> windows);

  RegionalOutlook history(List<OutlookWindow>? history);

  RegionalOutlook skill(RegionalOutlookSkill? skill);

  RegionalOutlook lowReportingDensity(bool? lowReportingDensity);

  RegionalOutlook lowDetectability(bool? lowDetectability);

  RegionalOutlook caveats(List<Caveat>? caveats);

  RegionalOutlook recordRef(String? recordRef);

  RegionalOutlook id(String? id);

  RegionalOutlook source_(String? source_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalOutlook(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalOutlook(...).copyWith(id: 12, name: "My name")
  /// ```
  RegionalOutlook call({
    String region,
    RegionalOutlookRegionLevelEnum regionLevel,
    String family,
    String? category,
    DateTime publishedAt,
    String modelVersion,
    int? asOfSeq,
    String? manifestId,
    List<OutlookWindow> windows,
    List<OutlookWindow>? history,
    RegionalOutlookSkill? skill,
    bool? lowReportingDensity,
    bool? lowDetectability,
    List<Caveat>? caveats,
    String? recordRef,
    String? id,
    String? source_,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRegionalOutlook.copyWith(...)` or call `instanceOfRegionalOutlook.copyWith.fieldName(value)` for a single field.
class _$RegionalOutlookCWProxyImpl implements _$RegionalOutlookCWProxy {
  const _$RegionalOutlookCWProxyImpl(this._value);

  final RegionalOutlook _value;

  @override
  RegionalOutlook region(String region) => call(region: region);

  @override
  RegionalOutlook regionLevel(RegionalOutlookRegionLevelEnum regionLevel) =>
      call(regionLevel: regionLevel);

  @override
  RegionalOutlook family(String family) => call(family: family);

  @override
  RegionalOutlook category(String? category) => call(category: category);

  @override
  RegionalOutlook publishedAt(DateTime publishedAt) =>
      call(publishedAt: publishedAt);

  @override
  RegionalOutlook modelVersion(String modelVersion) =>
      call(modelVersion: modelVersion);

  @override
  RegionalOutlook asOfSeq(int? asOfSeq) => call(asOfSeq: asOfSeq);

  @override
  RegionalOutlook manifestId(String? manifestId) =>
      call(manifestId: manifestId);

  @override
  RegionalOutlook windows(List<OutlookWindow> windows) =>
      call(windows: windows);

  @override
  RegionalOutlook history(List<OutlookWindow>? history) =>
      call(history: history);

  @override
  RegionalOutlook skill(RegionalOutlookSkill? skill) => call(skill: skill);

  @override
  RegionalOutlook lowReportingDensity(bool? lowReportingDensity) =>
      call(lowReportingDensity: lowReportingDensity);

  @override
  RegionalOutlook lowDetectability(bool? lowDetectability) =>
      call(lowDetectability: lowDetectability);

  @override
  RegionalOutlook caveats(List<Caveat>? caveats) => call(caveats: caveats);

  @override
  RegionalOutlook recordRef(String? recordRef) => call(recordRef: recordRef);

  @override
  RegionalOutlook id(String? id) => call(id: id);

  @override
  RegionalOutlook source_(String? source_) => call(source_: source_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalOutlook(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalOutlook(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RegionalOutlook call({
    Object? region = const $CopyWithPlaceholder(),
    Object? regionLevel = const $CopyWithPlaceholder(),
    Object? family = const $CopyWithPlaceholder(),
    Object? category = const $CopyWithPlaceholder(),
    Object? publishedAt = const $CopyWithPlaceholder(),
    Object? modelVersion = const $CopyWithPlaceholder(),
    Object? asOfSeq = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
    Object? windows = const $CopyWithPlaceholder(),
    Object? history = const $CopyWithPlaceholder(),
    Object? skill = const $CopyWithPlaceholder(),
    Object? lowReportingDensity = const $CopyWithPlaceholder(),
    Object? lowDetectability = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
    Object? recordRef = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? source_ = const $CopyWithPlaceholder(),
  }) {
    return RegionalOutlook(
      region: region == const $CopyWithPlaceholder() || region == null
          ? _value.region
          // ignore: cast_nullable_to_non_nullable
          : region as String,
      regionLevel:
          regionLevel == const $CopyWithPlaceholder() || regionLevel == null
          ? _value.regionLevel
          // ignore: cast_nullable_to_non_nullable
          : regionLevel as RegionalOutlookRegionLevelEnum,
      family: family == const $CopyWithPlaceholder() || family == null
          ? _value.family
          // ignore: cast_nullable_to_non_nullable
          : family as String,
      category: category == const $CopyWithPlaceholder()
          ? _value.category
          // ignore: cast_nullable_to_non_nullable
          : category as String?,
      publishedAt:
          publishedAt == const $CopyWithPlaceholder() || publishedAt == null
          ? _value.publishedAt
          // ignore: cast_nullable_to_non_nullable
          : publishedAt as DateTime,
      modelVersion:
          modelVersion == const $CopyWithPlaceholder() || modelVersion == null
          ? _value.modelVersion
          // ignore: cast_nullable_to_non_nullable
          : modelVersion as String,
      asOfSeq: asOfSeq == const $CopyWithPlaceholder()
          ? _value.asOfSeq
          // ignore: cast_nullable_to_non_nullable
          : asOfSeq as int?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
      windows: windows == const $CopyWithPlaceholder() || windows == null
          ? _value.windows
          // ignore: cast_nullable_to_non_nullable
          : windows as List<OutlookWindow>,
      history: history == const $CopyWithPlaceholder()
          ? _value.history
          // ignore: cast_nullable_to_non_nullable
          : history as List<OutlookWindow>?,
      skill: skill == const $CopyWithPlaceholder()
          ? _value.skill
          // ignore: cast_nullable_to_non_nullable
          : skill as RegionalOutlookSkill?,
      lowReportingDensity: lowReportingDensity == const $CopyWithPlaceholder()
          ? _value.lowReportingDensity
          // ignore: cast_nullable_to_non_nullable
          : lowReportingDensity as bool?,
      lowDetectability: lowDetectability == const $CopyWithPlaceholder()
          ? _value.lowDetectability
          // ignore: cast_nullable_to_non_nullable
          : lowDetectability as bool?,
      caveats: caveats == const $CopyWithPlaceholder()
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>?,
      recordRef: recordRef == const $CopyWithPlaceholder()
          ? _value.recordRef
          // ignore: cast_nullable_to_non_nullable
          : recordRef as String?,
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      source_: source_ == const $CopyWithPlaceholder()
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as String?,
    );
  }
}

extension $RegionalOutlookCopyWith on RegionalOutlook {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRegionalOutlook.copyWith(...)` or `instanceOfRegionalOutlook.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RegionalOutlookCWProxy get copyWith => _$RegionalOutlookCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RegionalOutlook _$RegionalOutlookFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RegionalOutlook', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'region',
          'regionLevel',
          'family',
          'publishedAt',
          'modelVersion',
          'windows',
        ],
      );
      final val = RegionalOutlook(
        region: $checkedConvert('region', (v) => v as String),
        regionLevel: $checkedConvert(
          'regionLevel',
          (v) => $enumDecode(_$RegionalOutlookRegionLevelEnumEnumMap, v),
        ),
        family: $checkedConvert('family', (v) => v as String),
        category: $checkedConvert('category', (v) => v as String?),
        publishedAt: $checkedConvert(
          'publishedAt',
          (v) => DateTime.parse(v as String),
        ),
        modelVersion: $checkedConvert('modelVersion', (v) => v as String),
        asOfSeq: $checkedConvert('asOfSeq', (v) => (v as num?)?.toInt()),
        manifestId: $checkedConvert('manifestId', (v) => v as String?),
        windows: $checkedConvert(
          'windows',
          (v) => (v as List<dynamic>)
              .map((e) => OutlookWindow.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        history: $checkedConvert(
          'history',
          (v) => (v as List<dynamic>?)
              ?.map((e) => OutlookWindow.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        skill: $checkedConvert(
          'skill',
          (v) => v == null
              ? null
              : RegionalOutlookSkill.fromJson(v as Map<String, dynamic>),
        ),
        lowReportingDensity: $checkedConvert(
          'lowReportingDensity',
          (v) => v as bool?,
        ),
        lowDetectability: $checkedConvert(
          'lowDetectability',
          (v) => v as bool?,
        ),
        caveats: $checkedConvert(
          'caveats',
          (v) => (v as List<dynamic>?)
              ?.map((e) => Caveat.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        recordRef: $checkedConvert('recordRef', (v) => v as String?),
        id: $checkedConvert('id', (v) => v as String?),
        source_: $checkedConvert('source', (v) => v as String?),
      );
      return val;
    }, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$RegionalOutlookToJson(
  RegionalOutlook instance,
) => <String, dynamic>{
  'region': instance.region,
  'regionLevel': _$RegionalOutlookRegionLevelEnumEnumMap[instance.regionLevel]!,
  'family': instance.family,
  'category': ?instance.category,
  'publishedAt': instance.publishedAt.toIso8601String(),
  'modelVersion': instance.modelVersion,
  'asOfSeq': ?instance.asOfSeq,
  'manifestId': ?instance.manifestId,
  'windows': instance.windows.map((e) => e.toJson()).toList(),
  'history': ?instance.history?.map((e) => e.toJson()).toList(),
  'skill': ?instance.skill?.toJson(),
  'lowReportingDensity': ?instance.lowReportingDensity,
  'lowDetectability': ?instance.lowDetectability,
  'caveats': ?instance.caveats?.map((e) => e.toJson()).toList(),
  'recordRef': ?instance.recordRef,
  'id': ?instance.id,
  'source': ?instance.source_,
};

const _$RegionalOutlookRegionLevelEnumEnumMap = {
  RegionalOutlookRegionLevelEnum.country: 'country',
  RegionalOutlookRegionLevelEnum.admin1: 'admin1',
};
