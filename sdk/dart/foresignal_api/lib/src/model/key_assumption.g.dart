// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'key_assumption.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$KeyAssumptionCWProxy {
  KeyAssumption id(String id);

  KeyAssumption setId(String setId);

  KeyAssumption text(String text);

  KeyAssumption linchpin(bool linchpin);

  KeyAssumption rationale(String? rationale);

  KeyAssumption wouldBeWrongIf(String? wouldBeWrongIf);

  KeyAssumption status(KeyAssumptionStatusEnum status);

  KeyAssumption indicatorIds(List<String>? indicatorIds);

  KeyAssumption reviewDue(DateTime? reviewDue);

  KeyAssumption updatedBy(String? updatedBy);

  KeyAssumption updatedAt(DateTime updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `KeyAssumption(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// KeyAssumption(...).copyWith(id: 12, name: "My name")
  /// ```
  KeyAssumption call({
    String id,
    String setId,
    String text,
    bool linchpin,
    String? rationale,
    String? wouldBeWrongIf,
    KeyAssumptionStatusEnum status,
    List<String>? indicatorIds,
    DateTime? reviewDue,
    String? updatedBy,
    DateTime updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfKeyAssumption.copyWith(...)` or call `instanceOfKeyAssumption.copyWith.fieldName(value)` for a single field.
class _$KeyAssumptionCWProxyImpl implements _$KeyAssumptionCWProxy {
  const _$KeyAssumptionCWProxyImpl(this._value);

  final KeyAssumption _value;

  @override
  KeyAssumption id(String id) => call(id: id);

  @override
  KeyAssumption setId(String setId) => call(setId: setId);

  @override
  KeyAssumption text(String text) => call(text: text);

  @override
  KeyAssumption linchpin(bool linchpin) => call(linchpin: linchpin);

  @override
  KeyAssumption rationale(String? rationale) => call(rationale: rationale);

  @override
  KeyAssumption wouldBeWrongIf(String? wouldBeWrongIf) =>
      call(wouldBeWrongIf: wouldBeWrongIf);

  @override
  KeyAssumption status(KeyAssumptionStatusEnum status) => call(status: status);

  @override
  KeyAssumption indicatorIds(List<String>? indicatorIds) =>
      call(indicatorIds: indicatorIds);

  @override
  KeyAssumption reviewDue(DateTime? reviewDue) => call(reviewDue: reviewDue);

  @override
  KeyAssumption updatedBy(String? updatedBy) => call(updatedBy: updatedBy);

  @override
  KeyAssumption updatedAt(DateTime updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `KeyAssumption(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// KeyAssumption(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  KeyAssumption call({
    Object? id = const $CopyWithPlaceholder(),
    Object? setId = const $CopyWithPlaceholder(),
    Object? text = const $CopyWithPlaceholder(),
    Object? linchpin = const $CopyWithPlaceholder(),
    Object? rationale = const $CopyWithPlaceholder(),
    Object? wouldBeWrongIf = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? indicatorIds = const $CopyWithPlaceholder(),
    Object? reviewDue = const $CopyWithPlaceholder(),
    Object? updatedBy = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return KeyAssumption(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      setId: setId == const $CopyWithPlaceholder() || setId == null
          ? _value.setId
          // ignore: cast_nullable_to_non_nullable
          : setId as String,
      text: text == const $CopyWithPlaceholder() || text == null
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String,
      linchpin: linchpin == const $CopyWithPlaceholder() || linchpin == null
          ? _value.linchpin
          // ignore: cast_nullable_to_non_nullable
          : linchpin as bool,
      rationale: rationale == const $CopyWithPlaceholder()
          ? _value.rationale
          // ignore: cast_nullable_to_non_nullable
          : rationale as String?,
      wouldBeWrongIf: wouldBeWrongIf == const $CopyWithPlaceholder()
          ? _value.wouldBeWrongIf
          // ignore: cast_nullable_to_non_nullable
          : wouldBeWrongIf as String?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as KeyAssumptionStatusEnum,
      indicatorIds: indicatorIds == const $CopyWithPlaceholder()
          ? _value.indicatorIds
          // ignore: cast_nullable_to_non_nullable
          : indicatorIds as List<String>?,
      reviewDue: reviewDue == const $CopyWithPlaceholder()
          ? _value.reviewDue
          // ignore: cast_nullable_to_non_nullable
          : reviewDue as DateTime?,
      updatedBy: updatedBy == const $CopyWithPlaceholder()
          ? _value.updatedBy
          // ignore: cast_nullable_to_non_nullable
          : updatedBy as String?,
      updatedAt: updatedAt == const $CopyWithPlaceholder() || updatedAt == null
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime,
    );
  }
}

extension $KeyAssumptionCopyWith on KeyAssumption {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfKeyAssumption.copyWith(...)` or `instanceOfKeyAssumption.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$KeyAssumptionCWProxy get copyWith => _$KeyAssumptionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

KeyAssumption _$KeyAssumptionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('KeyAssumption', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'id',
          'setId',
          'text',
          'linchpin',
          'status',
          'updatedAt',
        ],
      );
      final val = KeyAssumption(
        id: $checkedConvert('id', (v) => v as String),
        setId: $checkedConvert('setId', (v) => v as String),
        text: $checkedConvert('text', (v) => v as String),
        linchpin: $checkedConvert('linchpin', (v) => v as bool),
        rationale: $checkedConvert('rationale', (v) => v as String?),
        wouldBeWrongIf: $checkedConvert('wouldBeWrongIf', (v) => v as String?),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$KeyAssumptionStatusEnumEnumMap, v),
        ),
        indicatorIds: $checkedConvert(
          'indicatorIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        reviewDue: $checkedConvert(
          'reviewDue',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        updatedBy: $checkedConvert('updatedBy', (v) => v as String?),
        updatedAt: $checkedConvert(
          'updatedAt',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$KeyAssumptionToJson(KeyAssumption instance) =>
    <String, dynamic>{
      'id': instance.id,
      'setId': instance.setId,
      'text': instance.text,
      'linchpin': instance.linchpin,
      'rationale': ?instance.rationale,
      'wouldBeWrongIf': ?instance.wouldBeWrongIf,
      'status': _$KeyAssumptionStatusEnumEnumMap[instance.status]!,
      'indicatorIds': ?instance.indicatorIds,
      'reviewDue': ?instance.reviewDue?.toIso8601String(),
      'updatedBy': ?instance.updatedBy,
      'updatedAt': instance.updatedAt.toIso8601String(),
    };

const _$KeyAssumptionStatusEnumEnumMap = {
  KeyAssumptionStatusEnum.solid: 'solid',
  KeyAssumptionStatusEnum.caveated: 'caveated',
  KeyAssumptionStatusEnum.unsupported: 'unsupported',
  KeyAssumptionStatusEnum.broken: 'broken',
};
