// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'investigation_export.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InvestigationExportCWProxy {
  InvestigationExport header(InvestigationExportHeaderEnum header);

  InvestigationExport generatedAt(DateTime generatedAt);

  InvestigationExport investigation(Investigation investigation);

  InvestigationExport attributions(List<String> attributions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InvestigationExport(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InvestigationExport(...).copyWith(id: 12, name: "My name")
  /// ```
  InvestigationExport call({
    InvestigationExportHeaderEnum header,
    DateTime generatedAt,
    Investigation investigation,
    List<String> attributions,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInvestigationExport.copyWith(...)` or call `instanceOfInvestigationExport.copyWith.fieldName(value)` for a single field.
class _$InvestigationExportCWProxyImpl implements _$InvestigationExportCWProxy {
  const _$InvestigationExportCWProxyImpl(this._value);

  final InvestigationExport _value;

  @override
  InvestigationExport header(InvestigationExportHeaderEnum header) =>
      call(header: header);

  @override
  InvestigationExport generatedAt(DateTime generatedAt) =>
      call(generatedAt: generatedAt);

  @override
  InvestigationExport investigation(Investigation investigation) =>
      call(investigation: investigation);

  @override
  InvestigationExport attributions(List<String> attributions) =>
      call(attributions: attributions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InvestigationExport(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InvestigationExport(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InvestigationExport call({
    Object? header = const $CopyWithPlaceholder(),
    Object? generatedAt = const $CopyWithPlaceholder(),
    Object? investigation = const $CopyWithPlaceholder(),
    Object? attributions = const $CopyWithPlaceholder(),
  }) {
    return InvestigationExport(
      header: header == const $CopyWithPlaceholder() || header == null
          ? _value.header
          // ignore: cast_nullable_to_non_nullable
          : header as InvestigationExportHeaderEnum,
      generatedAt:
          generatedAt == const $CopyWithPlaceholder() || generatedAt == null
          ? _value.generatedAt
          // ignore: cast_nullable_to_non_nullable
          : generatedAt as DateTime,
      investigation:
          investigation == const $CopyWithPlaceholder() || investigation == null
          ? _value.investigation
          // ignore: cast_nullable_to_non_nullable
          : investigation as Investigation,
      attributions:
          attributions == const $CopyWithPlaceholder() || attributions == null
          ? _value.attributions
          // ignore: cast_nullable_to_non_nullable
          : attributions as List<String>,
    );
  }
}

extension $InvestigationExportCopyWith on InvestigationExport {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInvestigationExport.copyWith(...)` or `instanceOfInvestigationExport.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InvestigationExportCWProxy get copyWith =>
      _$InvestigationExportCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InvestigationExport _$InvestigationExportFromJson(Map<String, dynamic> json) =>
    $checkedCreate('InvestigationExport', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'header',
          'generatedAt',
          'investigation',
          'attributions',
        ],
      );
      final val = InvestigationExport(
        header: $checkedConvert(
          'header',
          (v) => $enumDecode(_$InvestigationExportHeaderEnumEnumMap, v),
        ),
        generatedAt: $checkedConvert(
          'generatedAt',
          (v) => DateTime.parse(v as String),
        ),
        investigation: $checkedConvert(
          'investigation',
          (v) => Investigation.fromJson(v as Map<String, dynamic>),
        ),
        attributions: $checkedConvert(
          'attributions',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$InvestigationExportToJson(
  InvestigationExport instance,
) => <String, dynamic>{
  'header': _$InvestigationExportHeaderEnumEnumMap[instance.header]!,
  'generatedAt': instance.generatedAt.toIso8601String(),
  'investigation': instance.investigation.toJson(),
  'attributions': instance.attributions,
};

const _$InvestigationExportHeaderEnumEnumMap = {
  InvestigationExportHeaderEnum
          .automatedAnalysisInputsSemicolonVerifyBeforeActing:
      'Automated analysis inputs; verify before acting',
};
