// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ask_progress_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AskProgressEventCWProxy {
  AskProgressEvent tool(String tool);

  AskProgressEvent status(AskProgressEventStatusEnum status);

  AskProgressEvent summary(String? summary);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskProgressEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskProgressEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  AskProgressEvent call({
    String tool,
    AskProgressEventStatusEnum status,
    String? summary,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAskProgressEvent.copyWith(...)` or call `instanceOfAskProgressEvent.copyWith.fieldName(value)` for a single field.
class _$AskProgressEventCWProxyImpl implements _$AskProgressEventCWProxy {
  const _$AskProgressEventCWProxyImpl(this._value);

  final AskProgressEvent _value;

  @override
  AskProgressEvent tool(String tool) => call(tool: tool);

  @override
  AskProgressEvent status(AskProgressEventStatusEnum status) =>
      call(status: status);

  @override
  AskProgressEvent summary(String? summary) => call(summary: summary);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskProgressEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskProgressEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AskProgressEvent call({
    Object? tool = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? summary = const $CopyWithPlaceholder(),
  }) {
    return AskProgressEvent(
      tool: tool == const $CopyWithPlaceholder() || tool == null
          ? _value.tool
          // ignore: cast_nullable_to_non_nullable
          : tool as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as AskProgressEventStatusEnum,
      summary: summary == const $CopyWithPlaceholder()
          ? _value.summary
          // ignore: cast_nullable_to_non_nullable
          : summary as String?,
    );
  }
}

extension $AskProgressEventCopyWith on AskProgressEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAskProgressEvent.copyWith(...)` or `instanceOfAskProgressEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AskProgressEventCWProxy get copyWith => _$AskProgressEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AskProgressEvent _$AskProgressEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AskProgressEvent', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['tool', 'status']);
      final val = AskProgressEvent(
        tool: $checkedConvert('tool', (v) => v as String),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$AskProgressEventStatusEnumEnumMap, v),
        ),
        summary: $checkedConvert('summary', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$AskProgressEventToJson(AskProgressEvent instance) =>
    <String, dynamic>{
      'tool': instance.tool,
      'status': _$AskProgressEventStatusEnumEnumMap[instance.status]!,
      'summary': ?instance.summary,
    };

const _$AskProgressEventStatusEnumEnumMap = {
  AskProgressEventStatusEnum.calling: 'calling',
  AskProgressEventStatusEnum.done: 'done',
};
