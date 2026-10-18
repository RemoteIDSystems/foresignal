// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ask_action.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AskActionCWProxy {
  AskAction id(String id);

  AskAction type(AskActionTypeEnum type);

  AskAction description(String description);

  AskAction payload(Map<String, Object>? payload);

  AskAction status(AskActionStatusEnum status);

  AskAction result(Map<String, Object>? result);

  AskAction createdAt(DateTime? createdAt);

  AskAction executedAt(DateTime? executedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskAction(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskAction(...).copyWith(id: 12, name: "My name")
  /// ```
  AskAction call({
    String id,
    AskActionTypeEnum type,
    String description,
    Map<String, Object>? payload,
    AskActionStatusEnum status,
    Map<String, Object>? result,
    DateTime? createdAt,
    DateTime? executedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAskAction.copyWith(...)` or call `instanceOfAskAction.copyWith.fieldName(value)` for a single field.
class _$AskActionCWProxyImpl implements _$AskActionCWProxy {
  const _$AskActionCWProxyImpl(this._value);

  final AskAction _value;

  @override
  AskAction id(String id) => call(id: id);

  @override
  AskAction type(AskActionTypeEnum type) => call(type: type);

  @override
  AskAction description(String description) => call(description: description);

  @override
  AskAction payload(Map<String, Object>? payload) => call(payload: payload);

  @override
  AskAction status(AskActionStatusEnum status) => call(status: status);

  @override
  AskAction result(Map<String, Object>? result) => call(result: result);

  @override
  AskAction createdAt(DateTime? createdAt) => call(createdAt: createdAt);

  @override
  AskAction executedAt(DateTime? executedAt) => call(executedAt: executedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskAction(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskAction(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AskAction call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? payload = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? result = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? executedAt = const $CopyWithPlaceholder(),
  }) {
    return AskAction(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as AskActionTypeEnum,
      description:
          description == const $CopyWithPlaceholder() || description == null
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String,
      payload: payload == const $CopyWithPlaceholder()
          ? _value.payload
          // ignore: cast_nullable_to_non_nullable
          : payload as Map<String, Object>?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as AskActionStatusEnum,
      result: result == const $CopyWithPlaceholder()
          ? _value.result
          // ignore: cast_nullable_to_non_nullable
          : result as Map<String, Object>?,
      createdAt: createdAt == const $CopyWithPlaceholder()
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime?,
      executedAt: executedAt == const $CopyWithPlaceholder()
          ? _value.executedAt
          // ignore: cast_nullable_to_non_nullable
          : executedAt as DateTime?,
    );
  }
}

extension $AskActionCopyWith on AskAction {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAskAction.copyWith(...)` or `instanceOfAskAction.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AskActionCWProxy get copyWith => _$AskActionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AskAction _$AskActionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AskAction', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'type', 'description', 'status']);
  final val = AskAction(
    id: $checkedConvert('id', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$AskActionTypeEnumEnumMap, v),
    ),
    description: $checkedConvert('description', (v) => v as String),
    payload: $checkedConvert(
      'payload',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$AskActionStatusEnumEnumMap, v),
    ),
    result: $checkedConvert(
      'result',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    createdAt: $checkedConvert(
      'createdAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    executedAt: $checkedConvert(
      'executedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$AskActionToJson(AskAction instance) => <String, dynamic>{
  'id': instance.id,
  'type': _$AskActionTypeEnumEnumMap[instance.type]!,
  'description': instance.description,
  'payload': ?instance.payload,
  'status': _$AskActionStatusEnumEnumMap[instance.status]!,
  'result': ?instance.result,
  'createdAt': ?instance.createdAt?.toIso8601String(),
  'executedAt': ?instance.executedAt?.toIso8601String(),
};

const _$AskActionTypeEnumEnumMap = {
  AskActionTypeEnum.createWatch: 'create_watch',
  AskActionTypeEnum.updateWatch: 'update_watch',
  AskActionTypeEnum.createBriefing: 'create_briefing',
  AskActionTypeEnum.acknowledgeAlert: 'acknowledge_alert',
  AskActionTypeEnum.alertStatus: 'alert_status',
  AskActionTypeEnum.snoozeAlert: 'snooze_alert',
  AskActionTypeEnum.createView: 'create_view',
  AskActionTypeEnum.setAlertFeedback: 'set_alert_feedback',
  AskActionTypeEnum.applyTuningSuggestion: 'apply_tuning_suggestion',
};

const _$AskActionStatusEnumEnumMap = {
  AskActionStatusEnum.pending: 'pending',
  AskActionStatusEnum.executed: 'executed',
  AskActionStatusEnum.rejected: 'rejected',
  AskActionStatusEnum.failed: 'failed',
};
