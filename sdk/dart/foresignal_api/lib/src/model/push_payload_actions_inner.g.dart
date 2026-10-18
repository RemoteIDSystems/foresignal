// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'push_payload_actions_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PushPayloadActionsInnerCWProxy {
  PushPayloadActionsInner action(PushPayloadActionsInnerActionEnum action);

  PushPayloadActionsInner title(String title);

  PushPayloadActionsInner url(String url);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushPayloadActionsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushPayloadActionsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  PushPayloadActionsInner call({
    PushPayloadActionsInnerActionEnum action,
    String title,
    String url,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPushPayloadActionsInner.copyWith(...)` or call `instanceOfPushPayloadActionsInner.copyWith.fieldName(value)` for a single field.
class _$PushPayloadActionsInnerCWProxyImpl
    implements _$PushPayloadActionsInnerCWProxy {
  const _$PushPayloadActionsInnerCWProxyImpl(this._value);

  final PushPayloadActionsInner _value;

  @override
  PushPayloadActionsInner action(PushPayloadActionsInnerActionEnum action) =>
      call(action: action);

  @override
  PushPayloadActionsInner title(String title) => call(title: title);

  @override
  PushPayloadActionsInner url(String url) => call(url: url);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushPayloadActionsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushPayloadActionsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PushPayloadActionsInner call({
    Object? action = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
  }) {
    return PushPayloadActionsInner(
      action: action == const $CopyWithPlaceholder() || action == null
          ? _value.action
          // ignore: cast_nullable_to_non_nullable
          : action as PushPayloadActionsInnerActionEnum,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      url: url == const $CopyWithPlaceholder() || url == null
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String,
    );
  }
}

extension $PushPayloadActionsInnerCopyWith on PushPayloadActionsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPushPayloadActionsInner.copyWith(...)` or `instanceOfPushPayloadActionsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PushPayloadActionsInnerCWProxy get copyWith =>
      _$PushPayloadActionsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PushPayloadActionsInner _$PushPayloadActionsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PushPayloadActionsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['action', 'title', 'url']);
  final val = PushPayloadActionsInner(
    action: $checkedConvert(
      'action',
      (v) => $enumDecode(_$PushPayloadActionsInnerActionEnumEnumMap, v),
    ),
    title: $checkedConvert('title', (v) => v as String),
    url: $checkedConvert('url', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$PushPayloadActionsInnerToJson(
  PushPayloadActionsInner instance,
) => <String, dynamic>{
  'action': _$PushPayloadActionsInnerActionEnumEnumMap[instance.action]!,
  'title': instance.title,
  'url': instance.url,
};

const _$PushPayloadActionsInnerActionEnumEnumMap = {
  PushPayloadActionsInnerActionEnum.ack: 'ack',
  PushPayloadActionsInnerActionEnum.snooze: 'snooze',
  PushPayloadActionsInnerActionEnum.mute: 'mute',
  PushPayloadActionsInnerActionEnum.feedbackColonUseful: 'feedback:useful',
  PushPayloadActionsInnerActionEnum.feedbackColonNoise: 'feedback:noise',
};
