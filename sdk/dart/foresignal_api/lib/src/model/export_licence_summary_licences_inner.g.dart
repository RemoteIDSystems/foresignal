// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'export_licence_summary_licences_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExportLicenceSummaryLicencesInnerCWProxy {
  ExportLicenceSummaryLicencesInner terms(LicenceTerms terms);

  ExportLicenceSummaryLicencesInner sources(List<String> sources);

  ExportLicenceSummaryLicencesInner rows(int rows);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExportLicenceSummaryLicencesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExportLicenceSummaryLicencesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ExportLicenceSummaryLicencesInner call({
    LicenceTerms terms,
    List<String> sources,
    int rows,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExportLicenceSummaryLicencesInner.copyWith(...)` or call `instanceOfExportLicenceSummaryLicencesInner.copyWith.fieldName(value)` for a single field.
class _$ExportLicenceSummaryLicencesInnerCWProxyImpl
    implements _$ExportLicenceSummaryLicencesInnerCWProxy {
  const _$ExportLicenceSummaryLicencesInnerCWProxyImpl(this._value);

  final ExportLicenceSummaryLicencesInner _value;

  @override
  ExportLicenceSummaryLicencesInner terms(LicenceTerms terms) =>
      call(terms: terms);

  @override
  ExportLicenceSummaryLicencesInner sources(List<String> sources) =>
      call(sources: sources);

  @override
  ExportLicenceSummaryLicencesInner rows(int rows) => call(rows: rows);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExportLicenceSummaryLicencesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExportLicenceSummaryLicencesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExportLicenceSummaryLicencesInner call({
    Object? terms = const $CopyWithPlaceholder(),
    Object? sources = const $CopyWithPlaceholder(),
    Object? rows = const $CopyWithPlaceholder(),
  }) {
    return ExportLicenceSummaryLicencesInner(
      terms: terms == const $CopyWithPlaceholder() || terms == null
          ? _value.terms
          // ignore: cast_nullable_to_non_nullable
          : terms as LicenceTerms,
      sources: sources == const $CopyWithPlaceholder() || sources == null
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<String>,
      rows: rows == const $CopyWithPlaceholder() || rows == null
          ? _value.rows
          // ignore: cast_nullable_to_non_nullable
          : rows as int,
    );
  }
}

extension $ExportLicenceSummaryLicencesInnerCopyWith
    on ExportLicenceSummaryLicencesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExportLicenceSummaryLicencesInner.copyWith(...)` or `instanceOfExportLicenceSummaryLicencesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExportLicenceSummaryLicencesInnerCWProxy get copyWith =>
      _$ExportLicenceSummaryLicencesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExportLicenceSummaryLicencesInner _$ExportLicenceSummaryLicencesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExportLicenceSummaryLicencesInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['terms', 'sources', 'rows']);
  final val = ExportLicenceSummaryLicencesInner(
    terms: $checkedConvert(
      'terms',
      (v) => LicenceTerms.fromJson(v as Map<String, dynamic>),
    ),
    sources: $checkedConvert(
      'sources',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    rows: $checkedConvert('rows', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$ExportLicenceSummaryLicencesInnerToJson(
  ExportLicenceSummaryLicencesInner instance,
) => <String, dynamic>{
  'terms': instance.terms.toJson(),
  'sources': instance.sources,
  'rows': instance.rows,
};
