// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'external_catalogue_entry_time_dimension.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExternalCatalogueEntryTimeDimensionCWProxy {
  ExternalCatalogueEntryTimeDimension format(String? format);

  ExternalCatalogueEntryTimeDimension period(String? period);

  ExternalCatalogueEntryTimeDimension typicalLatency(String? typicalLatency);

  ExternalCatalogueEntryTimeDimension defaultLag(String? defaultLag);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalCatalogueEntryTimeDimension(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalCatalogueEntryTimeDimension(...).copyWith(id: 12, name: "My name")
  /// ```
  ExternalCatalogueEntryTimeDimension call({
    String? format,
    String? period,
    String? typicalLatency,
    String? defaultLag,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExternalCatalogueEntryTimeDimension.copyWith(...)` or call `instanceOfExternalCatalogueEntryTimeDimension.copyWith.fieldName(value)` for a single field.
class _$ExternalCatalogueEntryTimeDimensionCWProxyImpl
    implements _$ExternalCatalogueEntryTimeDimensionCWProxy {
  const _$ExternalCatalogueEntryTimeDimensionCWProxyImpl(this._value);

  final ExternalCatalogueEntryTimeDimension _value;

  @override
  ExternalCatalogueEntryTimeDimension format(String? format) =>
      call(format: format);

  @override
  ExternalCatalogueEntryTimeDimension period(String? period) =>
      call(period: period);

  @override
  ExternalCatalogueEntryTimeDimension typicalLatency(String? typicalLatency) =>
      call(typicalLatency: typicalLatency);

  @override
  ExternalCatalogueEntryTimeDimension defaultLag(String? defaultLag) =>
      call(defaultLag: defaultLag);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalCatalogueEntryTimeDimension(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalCatalogueEntryTimeDimension(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExternalCatalogueEntryTimeDimension call({
    Object? format = const $CopyWithPlaceholder(),
    Object? period = const $CopyWithPlaceholder(),
    Object? typicalLatency = const $CopyWithPlaceholder(),
    Object? defaultLag = const $CopyWithPlaceholder(),
  }) {
    return ExternalCatalogueEntryTimeDimension(
      format: format == const $CopyWithPlaceholder()
          ? _value.format
          // ignore: cast_nullable_to_non_nullable
          : format as String?,
      period: period == const $CopyWithPlaceholder()
          ? _value.period
          // ignore: cast_nullable_to_non_nullable
          : period as String?,
      typicalLatency: typicalLatency == const $CopyWithPlaceholder()
          ? _value.typicalLatency
          // ignore: cast_nullable_to_non_nullable
          : typicalLatency as String?,
      defaultLag: defaultLag == const $CopyWithPlaceholder()
          ? _value.defaultLag
          // ignore: cast_nullable_to_non_nullable
          : defaultLag as String?,
    );
  }
}

extension $ExternalCatalogueEntryTimeDimensionCopyWith
    on ExternalCatalogueEntryTimeDimension {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExternalCatalogueEntryTimeDimension.copyWith(...)` or `instanceOfExternalCatalogueEntryTimeDimension.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExternalCatalogueEntryTimeDimensionCWProxy get copyWith =>
      _$ExternalCatalogueEntryTimeDimensionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExternalCatalogueEntryTimeDimension
_$ExternalCatalogueEntryTimeDimensionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ExternalCatalogueEntryTimeDimension', json, (
      $checkedConvert,
    ) {
      final val = ExternalCatalogueEntryTimeDimension(
        format: $checkedConvert('format', (v) => v as String?),
        period: $checkedConvert('period', (v) => v as String?),
        typicalLatency: $checkedConvert('typicalLatency', (v) => v as String?),
        defaultLag: $checkedConvert('defaultLag', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ExternalCatalogueEntryTimeDimensionToJson(
  ExternalCatalogueEntryTimeDimension instance,
) => <String, dynamic>{
  'format': ?instance.format,
  'period': ?instance.period,
  'typicalLatency': ?instance.typicalLatency,
  'defaultLag': ?instance.defaultLag,
};
