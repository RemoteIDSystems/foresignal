// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'investigation_item_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InvestigationItemInputCWProxy {
  InvestigationItemInput itemType(InvestigationItemInputItemTypeEnum itemType);

  InvestigationItemInput ref(String ref);

  InvestigationItemInput seq(int? seq);

  InvestigationItemInput note(String? note);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InvestigationItemInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InvestigationItemInput(...).copyWith(id: 12, name: "My name")
  /// ```
  InvestigationItemInput call({
    InvestigationItemInputItemTypeEnum itemType,
    String ref,
    int? seq,
    String? note,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInvestigationItemInput.copyWith(...)` or call `instanceOfInvestigationItemInput.copyWith.fieldName(value)` for a single field.
class _$InvestigationItemInputCWProxyImpl
    implements _$InvestigationItemInputCWProxy {
  const _$InvestigationItemInputCWProxyImpl(this._value);

  final InvestigationItemInput _value;

  @override
  InvestigationItemInput itemType(
    InvestigationItemInputItemTypeEnum itemType,
  ) => call(itemType: itemType);

  @override
  InvestigationItemInput ref(String ref) => call(ref: ref);

  @override
  InvestigationItemInput seq(int? seq) => call(seq: seq);

  @override
  InvestigationItemInput note(String? note) => call(note: note);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InvestigationItemInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InvestigationItemInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InvestigationItemInput call({
    Object? itemType = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? seq = const $CopyWithPlaceholder(),
    Object? note = const $CopyWithPlaceholder(),
  }) {
    return InvestigationItemInput(
      itemType: itemType == const $CopyWithPlaceholder() || itemType == null
          ? _value.itemType
          // ignore: cast_nullable_to_non_nullable
          : itemType as InvestigationItemInputItemTypeEnum,
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      seq: seq == const $CopyWithPlaceholder()
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int?,
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
    );
  }
}

extension $InvestigationItemInputCopyWith on InvestigationItemInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInvestigationItemInput.copyWith(...)` or `instanceOfInvestigationItemInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InvestigationItemInputCWProxy get copyWith =>
      _$InvestigationItemInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InvestigationItemInput _$InvestigationItemInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('InvestigationItemInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['itemType', 'ref']);
  final val = InvestigationItemInput(
    itemType: $checkedConvert(
      'itemType',
      (v) => $enumDecode(_$InvestigationItemInputItemTypeEnumEnumMap, v),
    ),
    ref: $checkedConvert('ref', (v) => v as String),
    seq: $checkedConvert('seq', (v) => (v as num?)?.toInt()),
    note: $checkedConvert('note', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$InvestigationItemInputToJson(
  InvestigationItemInput instance,
) => <String, dynamic>{
  'itemType': _$InvestigationItemInputItemTypeEnumEnumMap[instance.itemType]!,
  'ref': instance.ref,
  'seq': ?instance.seq,
  'note': ?instance.note,
};

const _$InvestigationItemInputItemTypeEnumEnumMap = {
  InvestigationItemInputItemTypeEnum.event: 'event',
  InvestigationItemInputItemTypeEnum.alert: 'alert',
  InvestigationItemInputItemTypeEnum.record: 'record',
  InvestigationItemInputItemTypeEnum.anomaly: 'anomaly',
  InvestigationItemInputItemTypeEnum.place: 'place',
  InvestigationItemInputItemTypeEnum.briefingRun: 'briefing_run',
  InvestigationItemInputItemTypeEnum.note: 'note',
  InvestigationItemInputItemTypeEnum.evidenceItem: 'evidence-item',
  InvestigationItemInputItemTypeEnum.attachment: 'attachment',
  InvestigationItemInputItemTypeEnum.object: 'object',
};
