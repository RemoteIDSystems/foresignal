// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'key_assumption_patch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$KeyAssumptionPatchCWProxy {
  KeyAssumptionPatch text(String? text);

  KeyAssumptionPatch linchpin(bool? linchpin);

  KeyAssumptionPatch rationale(String? rationale);

  KeyAssumptionPatch wouldBeWrongIf(String? wouldBeWrongIf);

  KeyAssumptionPatch status(KeyAssumptionPatchStatusEnum? status);

  KeyAssumptionPatch indicatorIds(List<String>? indicatorIds);

  KeyAssumptionPatch reviewDue(DateTime? reviewDue);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `KeyAssumptionPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// KeyAssumptionPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  KeyAssumptionPatch call({
    String? text,
    bool? linchpin,
    String? rationale,
    String? wouldBeWrongIf,
    KeyAssumptionPatchStatusEnum? status,
    List<String>? indicatorIds,
    DateTime? reviewDue,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfKeyAssumptionPatch.copyWith(...)` or call `instanceOfKeyAssumptionPatch.copyWith.fieldName(value)` for a single field.
class _$KeyAssumptionPatchCWProxyImpl implements _$KeyAssumptionPatchCWProxy {
  const _$KeyAssumptionPatchCWProxyImpl(this._value);

  final KeyAssumptionPatch _value;

  @override
  KeyAssumptionPatch text(String? text) => call(text: text);

  @override
  KeyAssumptionPatch linchpin(bool? linchpin) => call(linchpin: linchpin);

  @override
  KeyAssumptionPatch rationale(String? rationale) => call(rationale: rationale);

  @override
  KeyAssumptionPatch wouldBeWrongIf(String? wouldBeWrongIf) =>
      call(wouldBeWrongIf: wouldBeWrongIf);

  @override
  KeyAssumptionPatch status(KeyAssumptionPatchStatusEnum? status) =>
      call(status: status);

  @override
  KeyAssumptionPatch indicatorIds(List<String>? indicatorIds) =>
      call(indicatorIds: indicatorIds);

  @override
  KeyAssumptionPatch reviewDue(DateTime? reviewDue) =>
      call(reviewDue: reviewDue);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `KeyAssumptionPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// KeyAssumptionPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  KeyAssumptionPatch call({
    Object? text = const $CopyWithPlaceholder(),
    Object? linchpin = const $CopyWithPlaceholder(),
    Object? rationale = const $CopyWithPlaceholder(),
    Object? wouldBeWrongIf = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? indicatorIds = const $CopyWithPlaceholder(),
    Object? reviewDue = const $CopyWithPlaceholder(),
  }) {
    return KeyAssumptionPatch(
      text: text == const $CopyWithPlaceholder()
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String?,
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
          : status as KeyAssumptionPatchStatusEnum?,
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

extension $KeyAssumptionPatchCopyWith on KeyAssumptionPatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfKeyAssumptionPatch.copyWith(...)` or `instanceOfKeyAssumptionPatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$KeyAssumptionPatchCWProxy get copyWith =>
      _$KeyAssumptionPatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

KeyAssumptionPatch _$KeyAssumptionPatchFromJson(Map<String, dynamic> json) =>
    $checkedCreate('KeyAssumptionPatch', json, ($checkedConvert) {
      final val = KeyAssumptionPatch(
        text: $checkedConvert('text', (v) => v as String?),
        linchpin: $checkedConvert('linchpin', (v) => v as bool?),
        rationale: $checkedConvert('rationale', (v) => v as String?),
        wouldBeWrongIf: $checkedConvert('wouldBeWrongIf', (v) => v as String?),
        status: $checkedConvert(
          'status',
          (v) => $enumDecodeNullable(_$KeyAssumptionPatchStatusEnumEnumMap, v),
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

Map<String, dynamic> _$KeyAssumptionPatchToJson(KeyAssumptionPatch instance) =>
    <String, dynamic>{
      'text': ?instance.text,
      'linchpin': ?instance.linchpin,
      'rationale': ?instance.rationale,
      'wouldBeWrongIf': ?instance.wouldBeWrongIf,
      'status': ?_$KeyAssumptionPatchStatusEnumEnumMap[instance.status],
      'indicatorIds': ?instance.indicatorIds,
      'reviewDue': ?instance.reviewDue?.toIso8601String(),
    };

const _$KeyAssumptionPatchStatusEnumEnumMap = {
  KeyAssumptionPatchStatusEnum.solid: 'solid',
  KeyAssumptionPatchStatusEnum.caveated: 'caveated',
  KeyAssumptionPatchStatusEnum.unsupported: 'unsupported',
  KeyAssumptionPatchStatusEnum.broken: 'broken',
};
