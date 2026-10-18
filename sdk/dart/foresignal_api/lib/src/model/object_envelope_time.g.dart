// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'object_envelope_time.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ObjectEnvelopeTimeCWProxy {
  ObjectEnvelopeTime start(DateTime? start);

  ObjectEnvelopeTime end(DateTime? end);

  ObjectEnvelopeTime observedAt(DateTime? observedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObjectEnvelopeTime(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObjectEnvelopeTime(...).copyWith(id: 12, name: "My name")
  /// ```
  ObjectEnvelopeTime call({
    DateTime? start,
    DateTime? end,
    DateTime? observedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfObjectEnvelopeTime.copyWith(...)` or call `instanceOfObjectEnvelopeTime.copyWith.fieldName(value)` for a single field.
class _$ObjectEnvelopeTimeCWProxyImpl implements _$ObjectEnvelopeTimeCWProxy {
  const _$ObjectEnvelopeTimeCWProxyImpl(this._value);

  final ObjectEnvelopeTime _value;

  @override
  ObjectEnvelopeTime start(DateTime? start) => call(start: start);

  @override
  ObjectEnvelopeTime end(DateTime? end) => call(end: end);

  @override
  ObjectEnvelopeTime observedAt(DateTime? observedAt) =>
      call(observedAt: observedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObjectEnvelopeTime(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObjectEnvelopeTime(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ObjectEnvelopeTime call({
    Object? start = const $CopyWithPlaceholder(),
    Object? end = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
  }) {
    return ObjectEnvelopeTime(
      start: start == const $CopyWithPlaceholder()
          ? _value.start
          // ignore: cast_nullable_to_non_nullable
          : start as DateTime?,
      end: end == const $CopyWithPlaceholder()
          ? _value.end
          // ignore: cast_nullable_to_non_nullable
          : end as DateTime?,
      observedAt: observedAt == const $CopyWithPlaceholder()
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime?,
    );
  }
}

extension $ObjectEnvelopeTimeCopyWith on ObjectEnvelopeTime {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfObjectEnvelopeTime.copyWith(...)` or `instanceOfObjectEnvelopeTime.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ObjectEnvelopeTimeCWProxy get copyWith =>
      _$ObjectEnvelopeTimeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ObjectEnvelopeTime _$ObjectEnvelopeTimeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ObjectEnvelopeTime', json, ($checkedConvert) {
      final val = ObjectEnvelopeTime(
        start: $checkedConvert(
          'start',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        end: $checkedConvert(
          'end',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        observedAt: $checkedConvert(
          'observedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ObjectEnvelopeTimeToJson(ObjectEnvelopeTime instance) =>
    <String, dynamic>{
      'start': ?instance.start?.toIso8601String(),
      'end': ?instance.end?.toIso8601String(),
      'observedAt': ?instance.observedAt?.toIso8601String(),
    };
