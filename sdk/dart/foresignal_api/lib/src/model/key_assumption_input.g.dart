// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'key_assumption_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$KeyAssumptionInputCWProxy {
  KeyAssumptionInput text(String text);

  KeyAssumptionInput linchpin(bool? linchpin);

  KeyAssumptionInput rationale(String? rationale);

  KeyAssumptionInput wouldBeWrongIf(String? wouldBeWrongIf);

  KeyAssumptionInput status(KeyAssumptionInputStatusEnum? status);

  KeyAssumptionInput indicatorIds(List<String>? indicatorIds);

  KeyAssumptionInput reviewDue(DateTime? reviewDue);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `KeyAssumptionInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// KeyAssumptionInput(...).copyWith(id: 12, name: "My name")
  /// ```
  KeyAssumptionInput call({
    String text,
    bool? linchpin,
    String? rationale,
    String? wouldBeWrongIf,
    KeyAssumptionInputStatusEnum? status,
    List<String>? indicatorIds,
    DateTime? reviewDue,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfKeyAssumptionInput.copyWith(...)` or call `instanceOfKeyAssumptionInput.copyWith.fieldName(value)` for a single field.
class _$KeyAssumptionInputCWProxyImpl implements _$KeyAssumptionInputCWProxy {
  const _$KeyAssumptionInputCWProxyImpl(this._value);

  final KeyAssumptionInput _value;

  @override
  KeyAssumptionInput text(String text) => call(text: text);

  @override
  KeyAssumptionInput linchpin(bool? linchpin) => call(linchpin: linchpin);

  @override
  KeyAssumptionInput rationale(String? rationale) => call(rationale: rationale);

  @override
  KeyAssumptionInput wouldBeWrongIf(String? wouldBeWrongIf) =>
      call(wouldBeWrongIf: wouldBeWrongIf);

  @override
  KeyAssumptionInput status(KeyAssumptionInputStatusEnum? status) =>
      call(status: status);

  @override
  KeyAssumptionInput indicatorIds(List<String>? indicatorIds) =>
      call(indicatorIds: indicatorIds);

  @override
  KeyAssumptionInput reviewDue(DateTime? reviewDue) =>
      call(reviewDue: reviewDue);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `KeyAssumptionInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// KeyAssumptionInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  KeyAssumptionInput call({
    Object? text = const $CopyWithPlaceholder(),
    Object? linchpin = const $CopyWithPlaceholder(),
    Object? rationale = const $CopyWithPlaceholder(),
    Object? wouldBeWrongIf = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? indicatorIds = const $CopyWithPlaceholder(),
    Object? reviewDue = const $CopyWithPlaceholder(),
  }) {
    return KeyAssumptionInput(
      text: text == const $CopyWithPlaceholder() || text == null
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String,
      linchpin: linchpin == const $CopyWithPlaceholder()
          ? _value.linchpin
          // ignore: cast_nullable_to_non_nullable
          : linchpin as bool?,
      rationale: rationale == const $CopyWithPlaceholder()
          ? _value.rationale
          // ignore: cast_nullable_to_non_nullable
          : rationale as String?,
      wouldBeWrongIf: wouldBeWrongIf == const $CopyWithPlaceholder()
          ? _value.wouldBeWrongIf
          // ignore: cast_nullable_to_non_nullable
          : wouldBeWrongIf as String?,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as KeyAssumptionInputStatusEnum?,
      indicatorIds: indicatorIds == const $CopyWithPlaceholder()
          ? _value.indicatorIds
          // ignore: cast_nullable_to_non_nullable
          : indicatorIds as List<String>?,
      reviewDue: reviewDue == const $CopyWithPlaceholder()
          ? _value.reviewDue
          // ignore: cast_nullable_to_non_nullable
          : reviewDue as DateTime?,
    );
  }
}

extension $KeyAssumptionInputCopyWith on KeyAssumptionInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfKeyAssumptionInput.copyWith(...)` or `instanceOfKeyAssumptionInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$KeyAssumptionInputCWProxy get copyWith =>
      _$KeyAssumptionInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

KeyAssumptionInput _$KeyAssumptionInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('KeyAssumptionInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['text']);
      final val = KeyAssumptionInput(
        text: $checkedConvert('text', (v) => v as String),
        linchpin: $checkedConvert('linchpin', (v) => v as bool? ?? false),
        rationale: $checkedConvert('rationale', (v) => v as String?),
        wouldBeWrongIf: $checkedConvert('wouldBeWrongIf', (v) => v as String?),
        status: $checkedConvert(
          'status',
          (v) => $enumDecodeNullable(_$KeyAssumptionInputStatusEnumEnumMap, v),
        ),
        indicatorIds: $checkedConvert(
          'indicatorIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        reviewDue: $checkedConvert(
          'reviewDue',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$KeyAssumptionInputToJson(KeyAssumptionInput instance) =>
    <String, dynamic>{
      'text': instance.text,
      'linchpin': ?instance.linchpin,
      'rationale': ?instance.rationale,
      'wouldBeWrongIf': ?instance.wouldBeWrongIf,
      'status': ?_$KeyAssumptionInputStatusEnumEnumMap[instance.status],
      'indicatorIds': ?instance.indicatorIds,
      'reviewDue': ?instance.reviewDue?.toIso8601String(),
    };

const _$KeyAssumptionInputStatusEnumEnumMap = {
  KeyAssumptionInputStatusEnum.solid: 'solid',
  KeyAssumptionInputStatusEnum.caveated: 'caveated',
  KeyAssumptionInputStatusEnum.unsupported: 'unsupported',
  KeyAssumptionInputStatusEnum.broken: 'broken',
};
