// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'export_licence_summary.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExportLicenceSummaryCWProxy {
  ExportLicenceSummary licences(
    List<ExportLicenceSummaryLicencesInner> licences,
  );

  ExportLicenceSummary attributions(List<Attribution> attributions);

  ExportLicenceSummary personalUseOnly(bool personalUseOnly);

  ExportLicenceSummary personalUseSources(List<String>? personalUseSources);

  ExportLicenceSummary excluded(
    List<ExportLicenceSummaryExcludedInner>? excluded,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExportLicenceSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExportLicenceSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  ExportLicenceSummary call({
    List<ExportLicenceSummaryLicencesInner> licences,
    List<Attribution> attributions,
    bool personalUseOnly,
    List<String>? personalUseSources,
    List<ExportLicenceSummaryExcludedInner>? excluded,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExportLicenceSummary.copyWith(...)` or call `instanceOfExportLicenceSummary.copyWith.fieldName(value)` for a single field.
class _$ExportLicenceSummaryCWProxyImpl
    implements _$ExportLicenceSummaryCWProxy {
  const _$ExportLicenceSummaryCWProxyImpl(this._value);

  final ExportLicenceSummary _value;

  @override
  ExportLicenceSummary licences(
    List<ExportLicenceSummaryLicencesInner> licences,
  ) => call(licences: licences);

  @override
  ExportLicenceSummary attributions(List<Attribution> attributions) =>
      call(attributions: attributions);

  @override
  ExportLicenceSummary personalUseOnly(bool personalUseOnly) =>
      call(personalUseOnly: personalUseOnly);

  @override
  ExportLicenceSummary personalUseSources(List<String>? personalUseSources) =>
      call(personalUseSources: personalUseSources);

  @override
  ExportLicenceSummary excluded(
    List<ExportLicenceSummaryExcludedInner>? excluded,
  ) => call(excluded: excluded);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExportLicenceSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExportLicenceSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExportLicenceSummary call({
    Object? licences = const $CopyWithPlaceholder(),
    Object? attributions = const $CopyWithPlaceholder(),
    Object? personalUseOnly = const $CopyWithPlaceholder(),
    Object? personalUseSources = const $CopyWithPlaceholder(),
    Object? excluded = const $CopyWithPlaceholder(),
  }) {
    return ExportLicenceSummary(
      licences: licences == const $CopyWithPlaceholder() || licences == null
          ? _value.licences
          // ignore: cast_nullable_to_non_nullable
          : licences as List<ExportLicenceSummaryLicencesInner>,
      attributions:
          attributions == const $CopyWithPlaceholder() || attributions == null
          ? _value.attributions
          // ignore: cast_nullable_to_non_nullable
          : attributions as List<Attribution>,
      personalUseOnly:
          personalUseOnly == const $CopyWithPlaceholder() ||
              personalUseOnly == null
          ? _value.personalUseOnly
          // ignore: cast_nullable_to_non_nullable
          : personalUseOnly as bool,
      personalUseSources: personalUseSources == const $CopyWithPlaceholder()
          ? _value.personalUseSources
          // ignore: cast_nullable_to_non_nullable
          : personalUseSources as List<String>?,
      excluded: excluded == const $CopyWithPlaceholder()
          ? _value.excluded
          // ignore: cast_nullable_to_non_nullable
          : excluded as List<ExportLicenceSummaryExcludedInner>?,
    );
  }
}

extension $ExportLicenceSummaryCopyWith on ExportLicenceSummary {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExportLicenceSummary.copyWith(...)` or `instanceOfExportLicenceSummary.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExportLicenceSummaryCWProxy get copyWith =>
      _$ExportLicenceSummaryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExportLicenceSummary _$ExportLicenceSummaryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExportLicenceSummary', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['licences', 'attributions', 'personalUseOnly'],
  );
  final val = ExportLicenceSummary(
    licences: $checkedConvert(
      'licences',
      (v) => (v as List<dynamic>)
          .map(
            (e) => ExportLicenceSummaryLicencesInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    attributions: $checkedConvert(
      'attributions',
      (v) => (v as List<dynamic>)
          .map((e) => Attribution.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    personalUseOnly: $checkedConvert('personalUseOnly', (v) => v as bool),
    personalUseSources: $checkedConvert(
      'personalUseSources',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    excluded: $checkedConvert(
      'excluded',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => ExportLicenceSummaryExcludedInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ExportLicenceSummaryToJson(
  ExportLicenceSummary instance,
) => <String, dynamic>{
  'licences': instance.licences.map((e) => e.toJson()).toList(),
  'attributions': instance.attributions.map((e) => e.toJson()).toList(),
  'personalUseOnly': instance.personalUseOnly,
  'personalUseSources': ?instance.personalUseSources,
  'excluded': ?instance.excluded?.map((e) => e.toJson()).toList(),
};
