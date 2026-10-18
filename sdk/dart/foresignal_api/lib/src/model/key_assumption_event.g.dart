// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'key_assumption_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$KeyAssumptionEventCWProxy {
  KeyAssumptionEvent assumptionId(String assumptionId);

  KeyAssumptionEvent setId(String setId);

  KeyAssumptionEvent text(String? text);

  KeyAssumptionEvent linchpin(bool? linchpin);

  KeyAssumptionEvent status(KeyAssumptionEventStatusEnum status);

  KeyAssumptionEvent previousStatus(
    KeyAssumptionEventPreviousStatusEnum? previousStatus,
  );

  KeyAssumptionEvent private(bool? private);

  KeyAssumptionEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `KeyAssumptionEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// KeyAssumptionEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  KeyAssumptionEvent call({
    String assumptionId,
    String setId,
    String? text,
    bool? linchpin,
    KeyAssumptionEventStatusEnum status,
    KeyAssumptionEventPreviousStatusEnum? previousStatus,
    bool? private,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfKeyAssumptionEvent.copyWith(...)` or call `instanceOfKeyAssumptionEvent.copyWith.fieldName(value)` for a single field.
class _$KeyAssumptionEventCWProxyImpl implements _$KeyAssumptionEventCWProxy {
  const _$KeyAssumptionEventCWProxyImpl(this._value);

  final KeyAssumptionEvent _value;

  @override
  KeyAssumptionEvent assumptionId(String assumptionId) =>
      call(assumptionId: assumptionId);

  @override
  KeyAssumptionEvent setId(String setId) => call(setId: setId);

  @override
  KeyAssumptionEvent text(String? text) => call(text: text);

  @override
  KeyAssumptionEvent linchpin(bool? linchpin) => call(linchpin: linchpin);

  @override
  KeyAssumptionEvent status(KeyAssumptionEventStatusEnum status) =>
      call(status: status);

  @override
  KeyAssumptionEvent previousStatus(
    KeyAssumptionEventPreviousStatusEnum? previousStatus,
  ) => call(previousStatus: previousStatus);

  @override
  KeyAssumptionEvent private(bool? private) => call(private: private);

  @override
  KeyAssumptionEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `KeyAssumptionEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// KeyAssumptionEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  KeyAssumptionEvent call({
    Object? assumptionId = const $CopyWithPlaceholder(),
    Object? setId = const $CopyWithPlaceholder(),
    Object? text = const $CopyWithPlaceholder(),
    Object? linchpin = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? previousStatus = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return KeyAssumptionEvent(
      assumptionId:
          assumptionId == const $CopyWithPlaceholder() || assumptionId == null
          ? _value.assumptionId
          // ignore: cast_nullable_to_non_nullable
          : assumptionId as String,
      setId: setId == const $CopyWithPlaceholder() || setId == null
          ? _value.setId
          // ignore: cast_nullable_to_non_nullable
          : setId as String,
      text: text == const $CopyWithPlaceholder()
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String?,
      linchpin: linchpin == const $CopyWithPlaceholder()
          ? _value.linchpin
          // ignore: cast_nullable_to_non_nullable
          : linchpin as bool?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as KeyAssumptionEventStatusEnum,
      previousStatus: previousStatus == const $CopyWithPlaceholder()
          ? _value.previousStatus
          // ignore: cast_nullable_to_non_nullable
          : previousStatus as KeyAssumptionEventPreviousStatusEnum?,
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool?,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $KeyAssumptionEventCopyWith on KeyAssumptionEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfKeyAssumptionEvent.copyWith(...)` or `instanceOfKeyAssumptionEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$KeyAssumptionEventCWProxy get copyWith =>
      _$KeyAssumptionEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

KeyAssumptionEvent _$KeyAssumptionEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('KeyAssumptionEvent', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['assumptionId', 'setId', 'status', 'at'],
      );
      final val = KeyAssumptionEvent(
        assumptionId: $checkedConvert('assumptionId', (v) => v as String),
        setId: $checkedConvert('setId', (v) => v as String),
        text: $checkedConvert('text', (v) => v as String?),
        linchpin: $checkedConvert('linchpin', (v) => v as bool?),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$KeyAssumptionEventStatusEnumEnumMap, v),
        ),
        previousStatus: $checkedConvert(
          'previousStatus',
          (v) => $enumDecodeNullable(
            _$KeyAssumptionEventPreviousStatusEnumEnumMap,
            v,
          ),
        ),
        private: $checkedConvert('private', (v) => v as bool?),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
      );
      return val;
    });

Map<String, dynamic> _$KeyAssumptionEventToJson(
  KeyAssumptionEvent instance,
) => <String, dynamic>{
  'assumptionId': instance.assumptionId,
  'setId': instance.setId,
  'text': ?instance.text,
  'linchpin': ?instance.linchpin,
  'status': _$KeyAssumptionEventStatusEnumEnumMap[instance.status]!,
  'previousStatus':
      ?_$KeyAssumptionEventPreviousStatusEnumEnumMap[instance.previousStatus],
  'private': ?instance.private,
  'at': instance.at.toIso8601String(),
};

const _$KeyAssumptionEventStatusEnumEnumMap = {
  KeyAssumptionEventStatusEnum.solid: 'solid',
  KeyAssumptionEventStatusEnum.caveated: 'caveated',
  KeyAssumptionEventStatusEnum.unsupported: 'unsupported',
  KeyAssumptionEventStatusEnum.broken: 'broken',
};

const _$KeyAssumptionEventPreviousStatusEnumEnumMap = {
  KeyAssumptionEventPreviousStatusEnum.solid: 'solid',
  KeyAssumptionEventPreviousStatusEnum.caveated: 'caveated',
  KeyAssumptionEventPreviousStatusEnum.unsupported: 'unsupported',
  KeyAssumptionEventPreviousStatusEnum.broken: 'broken',
};
