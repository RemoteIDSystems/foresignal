// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'taxii_envelope.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TaxiiEnvelopeCWProxy {
  TaxiiEnvelope more(bool? more);

  TaxiiEnvelope next(String? next);

  TaxiiEnvelope objects(List<Map<String, Object>>? objects);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TaxiiEnvelope(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TaxiiEnvelope(...).copyWith(id: 12, name: "My name")
  /// ```
  TaxiiEnvelope call({
    bool? more,
    String? next,
    List<Map<String, Object>>? objects,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTaxiiEnvelope.copyWith(...)` or call `instanceOfTaxiiEnvelope.copyWith.fieldName(value)` for a single field.
class _$TaxiiEnvelopeCWProxyImpl implements _$TaxiiEnvelopeCWProxy {
  const _$TaxiiEnvelopeCWProxyImpl(this._value);

  final TaxiiEnvelope _value;

  @override
  TaxiiEnvelope more(bool? more) => call(more: more);

  @override
  TaxiiEnvelope next(String? next) => call(next: next);

  @override
  TaxiiEnvelope objects(List<Map<String, Object>>? objects) =>
      call(objects: objects);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TaxiiEnvelope(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TaxiiEnvelope(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TaxiiEnvelope call({
    Object? more = const $CopyWithPlaceholder(),
    Object? next = const $CopyWithPlaceholder(),
    Object? objects = const $CopyWithPlaceholder(),
  }) {
    return TaxiiEnvelope(
      more: more == const $CopyWithPlaceholder()
          ? _value.more
          // ignore: cast_nullable_to_non_nullable
          : more as bool?,
      next: next == const $CopyWithPlaceholder()
          ? _value.next
          // ignore: cast_nullable_to_non_nullable
          : next as String?,
      objects: objects == const $CopyWithPlaceholder()
          ? _value.objects
          // ignore: cast_nullable_to_non_nullable
          : objects as List<Map<String, Object>>?,
    );
  }
}

extension $TaxiiEnvelopeCopyWith on TaxiiEnvelope {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTaxiiEnvelope.copyWith(...)` or `instanceOfTaxiiEnvelope.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TaxiiEnvelopeCWProxy get copyWith => _$TaxiiEnvelopeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TaxiiEnvelope _$TaxiiEnvelopeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TaxiiEnvelope', json, ($checkedConvert) {
      final val = TaxiiEnvelope(
        more: $checkedConvert('more', (v) => v as bool?),
        next: $checkedConvert('next', (v) => v as String?),
        objects: $checkedConvert(
          'objects',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => (e as Map<String, dynamic>).map(
                  (k, e) => MapEntry(k, e as Object),
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$TaxiiEnvelopeToJson(TaxiiEnvelope instance) =>
    <String, dynamic>{
      'more': ?instance.more,
      'next': ?instance.next,
      'objects': ?instance.objects,
    };
