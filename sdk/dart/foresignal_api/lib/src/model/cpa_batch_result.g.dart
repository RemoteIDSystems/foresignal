// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cpa_batch_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CpaBatchResultCWProxy {
  CpaBatchResult subject(KinematicState subject);

  CpaBatchResult asOf(DateTime asOf);

  CpaBatchResult evaluated(int evaluated);

  CpaBatchResult truncated(bool? truncated);

  CpaBatchResult items(List<CpaResult> items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CpaBatchResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CpaBatchResult(...).copyWith(id: 12, name: "My name")
  /// ```
  CpaBatchResult call({
    KinematicState subject,
    DateTime asOf,
    int evaluated,
    bool? truncated,
    List<CpaResult> items,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCpaBatchResult.copyWith(...)` or call `instanceOfCpaBatchResult.copyWith.fieldName(value)` for a single field.
class _$CpaBatchResultCWProxyImpl implements _$CpaBatchResultCWProxy {
  const _$CpaBatchResultCWProxyImpl(this._value);

  final CpaBatchResult _value;

  @override
  CpaBatchResult subject(KinematicState subject) => call(subject: subject);

  @override
  CpaBatchResult asOf(DateTime asOf) => call(asOf: asOf);

  @override
  CpaBatchResult evaluated(int evaluated) => call(evaluated: evaluated);

  @override
  CpaBatchResult truncated(bool? truncated) => call(truncated: truncated);

  @override
  CpaBatchResult items(List<CpaResult> items) => call(items: items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CpaBatchResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CpaBatchResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CpaBatchResult call({
    Object? subject = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? evaluated = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
  }) {
    return CpaBatchResult(
      subject: subject == const $CopyWithPlaceholder() || subject == null
          ? _value.subject
          // ignore: cast_nullable_to_non_nullable
          : subject as KinematicState,
      asOf: asOf == const $CopyWithPlaceholder() || asOf == null
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime,
      evaluated: evaluated == const $CopyWithPlaceholder() || evaluated == null
          ? _value.evaluated
          // ignore: cast_nullable_to_non_nullable
          : evaluated as int,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool?,
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<CpaResult>,
    );
  }
}

extension $CpaBatchResultCopyWith on CpaBatchResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCpaBatchResult.copyWith(...)` or `instanceOfCpaBatchResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CpaBatchResultCWProxy get copyWith => _$CpaBatchResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CpaBatchResult _$CpaBatchResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CpaBatchResult', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['subject', 'asOf', 'evaluated', 'items'],
      );
      final val = CpaBatchResult(
        subject: $checkedConvert(
          'subject',
          (v) => KinematicState.fromJson(v as Map<String, dynamic>),
        ),
        asOf: $checkedConvert('asOf', (v) => DateTime.parse(v as String)),
        evaluated: $checkedConvert('evaluated', (v) => (v as num).toInt()),
        truncated: $checkedConvert('truncated', (v) => v as bool?),
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => CpaResult.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$CpaBatchResultToJson(CpaBatchResult instance) =>
    <String, dynamic>{
      'subject': instance.subject.toJson(),
      'asOf': instance.asOf.toIso8601String(),
      'evaluated': instance.evaluated,
      'truncated': ?instance.truncated,
      'items': instance.items.map((e) => e.toJson()).toList(),
    };
