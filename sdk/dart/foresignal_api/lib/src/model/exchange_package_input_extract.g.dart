// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'exchange_package_input_extract.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExchangePackageInputExtractCWProxy {
  ExchangePackageInputExtract from(DateTime? from);

  ExchangePackageInputExtract to(DateTime? to);

  ExchangePackageInputExtract layers(List<String>? layers);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExchangePackageInputExtract(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExchangePackageInputExtract(...).copyWith(id: 12, name: "My name")
  /// ```
  ExchangePackageInputExtract call({
    DateTime? from,
    DateTime? to,
    List<String>? layers,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExchangePackageInputExtract.copyWith(...)` or call `instanceOfExchangePackageInputExtract.copyWith.fieldName(value)` for a single field.
class _$ExchangePackageInputExtractCWProxyImpl
    implements _$ExchangePackageInputExtractCWProxy {
  const _$ExchangePackageInputExtractCWProxyImpl(this._value);

  final ExchangePackageInputExtract _value;

  @override
  ExchangePackageInputExtract from(DateTime? from) => call(from: from);

  @override
  ExchangePackageInputExtract to(DateTime? to) => call(to: to);

  @override
  ExchangePackageInputExtract layers(List<String>? layers) =>
      call(layers: layers);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExchangePackageInputExtract(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExchangePackageInputExtract(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExchangePackageInputExtract call({
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? layers = const $CopyWithPlaceholder(),
  }) {
    return ExchangePackageInputExtract(
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime?,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime?,
      layers: layers == const $CopyWithPlaceholder()
          ? _value.layers
          // ignore: cast_nullable_to_non_nullable
          : layers as List<String>?,
    );
  }
}

extension $ExchangePackageInputExtractCopyWith on ExchangePackageInputExtract {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExchangePackageInputExtract.copyWith(...)` or `instanceOfExchangePackageInputExtract.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExchangePackageInputExtractCWProxy get copyWith =>
      _$ExchangePackageInputExtractCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExchangePackageInputExtract _$ExchangePackageInputExtractFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExchangePackageInputExtract', json, ($checkedConvert) {
  final val = ExchangePackageInputExtract(
    from: $checkedConvert(
      'from',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    to: $checkedConvert(
      'to',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    layers: $checkedConvert(
      'layers',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ExchangePackageInputExtractToJson(
  ExchangePackageInputExtract instance,
) => <String, dynamic>{
  'from': ?instance.from?.toIso8601String(),
  'to': ?instance.to?.toIso8601String(),
  'layers': ?instance.layers,
};
