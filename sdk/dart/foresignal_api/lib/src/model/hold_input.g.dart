// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hold_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HoldInputCWProxy {
  HoldInput name(String name);

  HoldInput reason(String reason);

  HoldInput sources(List<String>? sources);

  HoldInput kinds(List<String>? kinds);

  HoldInput from(DateTime? from);

  HoldInput to(DateTime? to);

  HoldInput fromSeq(int? fromSeq);

  HoldInput toSeq(int? toSeq);

  HoldInput expiresAt(DateTime? expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HoldInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HoldInput(...).copyWith(id: 12, name: "My name")
  /// ```
  HoldInput call({
    String name,
    String reason,
    List<String>? sources,
    List<String>? kinds,
    DateTime? from,
    DateTime? to,
    int? fromSeq,
    int? toSeq,
    DateTime? expiresAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHoldInput.copyWith(...)` or call `instanceOfHoldInput.copyWith.fieldName(value)` for a single field.
class _$HoldInputCWProxyImpl implements _$HoldInputCWProxy {
  const _$HoldInputCWProxyImpl(this._value);

  final HoldInput _value;

  @override
  HoldInput name(String name) => call(name: name);

  @override
  HoldInput reason(String reason) => call(reason: reason);

  @override
  HoldInput sources(List<String>? sources) => call(sources: sources);

  @override
  HoldInput kinds(List<String>? kinds) => call(kinds: kinds);

  @override
  HoldInput from(DateTime? from) => call(from: from);

  @override
  HoldInput to(DateTime? to) => call(to: to);

  @override
  HoldInput fromSeq(int? fromSeq) => call(fromSeq: fromSeq);

  @override
  HoldInput toSeq(int? toSeq) => call(toSeq: toSeq);

  @override
  HoldInput expiresAt(DateTime? expiresAt) => call(expiresAt: expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HoldInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HoldInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HoldInput call({
    Object? name = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? sources = const $CopyWithPlaceholder(),
    Object? kinds = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? fromSeq = const $CopyWithPlaceholder(),
    Object? toSeq = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
  }) {
    return HoldInput(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String,
      sources: sources == const $CopyWithPlaceholder()
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<String>?,
      kinds: kinds == const $CopyWithPlaceholder()
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as List<String>?,
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime?,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime?,
      fromSeq: fromSeq == const $CopyWithPlaceholder()
          ? _value.fromSeq
          // ignore: cast_nullable_to_non_nullable
          : fromSeq as int?,
      toSeq: toSeq == const $CopyWithPlaceholder()
          ? _value.toSeq
          // ignore: cast_nullable_to_non_nullable
          : toSeq as int?,
      expiresAt: expiresAt == const $CopyWithPlaceholder()
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime?,
    );
  }
}

extension $HoldInputCopyWith on HoldInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHoldInput.copyWith(...)` or `instanceOfHoldInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HoldInputCWProxy get copyWith => _$HoldInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HoldInput _$HoldInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('HoldInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['name', 'reason']);
      final val = HoldInput(
        name: $checkedConvert('name', (v) => v as String),
        reason: $checkedConvert('reason', (v) => v as String),
        sources: $checkedConvert(
          'sources',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        kinds: $checkedConvert(
          'kinds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        from: $checkedConvert(
          'from',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        to: $checkedConvert(
          'to',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        fromSeq: $checkedConvert('fromSeq', (v) => (v as num?)?.toInt()),
        toSeq: $checkedConvert('toSeq', (v) => (v as num?)?.toInt()),
        expiresAt: $checkedConvert(
          'expiresAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$HoldInputToJson(HoldInput instance) => <String, dynamic>{
  'name': instance.name,
  'reason': instance.reason,
  'sources': ?instance.sources,
  'kinds': ?instance.kinds,
  'from': ?instance.from?.toIso8601String(),
  'to': ?instance.to?.toIso8601String(),
  'fromSeq': ?instance.fromSeq,
  'toSeq': ?instance.toSeq,
  'expiresAt': ?instance.expiresAt?.toIso8601String(),
};
