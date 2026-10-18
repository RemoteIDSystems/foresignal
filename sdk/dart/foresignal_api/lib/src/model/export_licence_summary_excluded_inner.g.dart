// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'export_licence_summary_excluded_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExportLicenceSummaryExcludedInnerCWProxy {
  ExportLicenceSummaryExcludedInner sourceId(String sourceId);

  ExportLicenceSummaryExcludedInner reason(
    ExportLicenceSummaryExcludedInnerReasonEnum reason,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExportLicenceSummaryExcludedInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExportLicenceSummaryExcludedInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ExportLicenceSummaryExcludedInner call({
    String sourceId,
    ExportLicenceSummaryExcludedInnerReasonEnum reason,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExportLicenceSummaryExcludedInner.copyWith(...)` or call `instanceOfExportLicenceSummaryExcludedInner.copyWith.fieldName(value)` for a single field.
class _$ExportLicenceSummaryExcludedInnerCWProxyImpl
    implements _$ExportLicenceSummaryExcludedInnerCWProxy {
  const _$ExportLicenceSummaryExcludedInnerCWProxyImpl(this._value);

  final ExportLicenceSummaryExcludedInner _value;

  @override
  ExportLicenceSummaryExcludedInner sourceId(String sourceId) =>
      call(sourceId: sourceId);

  @override
  ExportLicenceSummaryExcludedInner reason(
    ExportLicenceSummaryExcludedInnerReasonEnum reason,
  ) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExportLicenceSummaryExcludedInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExportLicenceSummaryExcludedInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExportLicenceSummaryExcludedInner call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return ExportLicenceSummaryExcludedInner(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as ExportLicenceSummaryExcludedInnerReasonEnum,
    );
  }
}

extension $ExportLicenceSummaryExcludedInnerCopyWith
    on ExportLicenceSummaryExcludedInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExportLicenceSummaryExcludedInner.copyWith(...)` or `instanceOfExportLicenceSummaryExcludedInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExportLicenceSummaryExcludedInnerCWProxy get copyWith =>
      _$ExportLicenceSummaryExcludedInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExportLicenceSummaryExcludedInner _$ExportLicenceSummaryExcludedInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExportLicenceSummaryExcludedInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['sourceId', 'reason']);
  final val = ExportLicenceSummaryExcludedInner(
    sourceId: $checkedConvert('sourceId', (v) => v as String),
    reason: $checkedConvert(
      'reason',
      (v) =>
          $enumDecode(_$ExportLicenceSummaryExcludedInnerReasonEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$ExportLicenceSummaryExcludedInnerToJson(
  ExportLicenceSummaryExcludedInner instance,
) => <String, dynamic>{
  'sourceId': instance.sourceId,
  'reason':
      _$ExportLicenceSummaryExcludedInnerReasonEnumEnumMap[instance.reason]!,
};

const _$ExportLicenceSummaryExcludedInnerReasonEnumEnumMap = {
  ExportLicenceSummaryExcludedInnerReasonEnum.licenceForbidden:
      'licence-forbidden',
  ExportLicenceSummaryExcludedInnerReasonEnum.licenceLocalOnly:
      'licence-local-only',
  ExportLicenceSummaryExcludedInnerReasonEnum.private: 'private',
};
