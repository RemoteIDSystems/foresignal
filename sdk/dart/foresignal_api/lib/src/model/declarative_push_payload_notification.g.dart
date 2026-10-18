// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'declarative_push_payload_notification.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DeclarativePushPayloadNotificationCWProxy {
  DeclarativePushPayloadNotification title(String title);

  DeclarativePushPayloadNotification body(String? body);

  DeclarativePushPayloadNotification navigate(String navigate);

  DeclarativePushPayloadNotification lang(String? lang);

  DeclarativePushPayloadNotification tag(String? tag);

  DeclarativePushPayloadNotification silent(bool? silent);

  DeclarativePushPayloadNotification data(PushPayload? data);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DeclarativePushPayloadNotification(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DeclarativePushPayloadNotification(...).copyWith(id: 12, name: "My name")
  /// ```
  DeclarativePushPayloadNotification call({
    String title,
    String? body,
    String navigate,
    String? lang,
    String? tag,
    bool? silent,
    PushPayload? data,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDeclarativePushPayloadNotification.copyWith(...)` or call `instanceOfDeclarativePushPayloadNotification.copyWith.fieldName(value)` for a single field.
class _$DeclarativePushPayloadNotificationCWProxyImpl
    implements _$DeclarativePushPayloadNotificationCWProxy {
  const _$DeclarativePushPayloadNotificationCWProxyImpl(this._value);

  final DeclarativePushPayloadNotification _value;

  @override
  DeclarativePushPayloadNotification title(String title) => call(title: title);

  @override
  DeclarativePushPayloadNotification body(String? body) => call(body: body);

  @override
  DeclarativePushPayloadNotification navigate(String navigate) =>
      call(navigate: navigate);

  @override
  DeclarativePushPayloadNotification lang(String? lang) => call(lang: lang);

  @override
  DeclarativePushPayloadNotification tag(String? tag) => call(tag: tag);

  @override
  DeclarativePushPayloadNotification silent(bool? silent) =>
      call(silent: silent);

  @override
  DeclarativePushPayloadNotification data(PushPayload? data) =>
      call(data: data);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DeclarativePushPayloadNotification(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DeclarativePushPayloadNotification(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DeclarativePushPayloadNotification call({
    Object? title = const $CopyWithPlaceholder(),
    Object? body = const $CopyWithPlaceholder(),
    Object? navigate = const $CopyWithPlaceholder(),
    Object? lang = const $CopyWithPlaceholder(),
    Object? tag = const $CopyWithPlaceholder(),
    Object? silent = const $CopyWithPlaceholder(),
    Object? data = const $CopyWithPlaceholder(),
  }) {
    return DeclarativePushPayloadNotification(
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      body: body == const $CopyWithPlaceholder()
          ? _value.body
          // ignore: cast_nullable_to_non_nullable
          : body as String?,
      navigate: navigate == const $CopyWithPlaceholder() || navigate == null
          ? _value.navigate
          // ignore: cast_nullable_to_non_nullable
          : navigate as String,
      lang: lang == const $CopyWithPlaceholder()
          ? _value.lang
          // ignore: cast_nullable_to_non_nullable
          : lang as String?,
      tag: tag == const $CopyWithPlaceholder()
          ? _value.tag
          // ignore: cast_nullable_to_non_nullable
          : tag as String?,
      silent: silent == const $CopyWithPlaceholder()
          ? _value.silent
          // ignore: cast_nullable_to_non_nullable
          : silent as bool?,
      data: data == const $CopyWithPlaceholder()
          ? _value.data
          // ignore: cast_nullable_to_non_nullable
          : data as PushPayload?,
    );
  }
}

extension $DeclarativePushPayloadNotificationCopyWith
    on DeclarativePushPayloadNotification {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDeclarativePushPayloadNotification.copyWith(...)` or `instanceOfDeclarativePushPayloadNotification.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DeclarativePushPayloadNotificationCWProxy get copyWith =>
      _$DeclarativePushPayloadNotificationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DeclarativePushPayloadNotification _$DeclarativePushPayloadNotificationFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DeclarativePushPayloadNotification', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['title', 'navigate']);
  final val = DeclarativePushPayloadNotification(
    title: $checkedConvert('title', (v) => v as String),
    body: $checkedConvert('body', (v) => v as String?),
    navigate: $checkedConvert('navigate', (v) => v as String),
    lang: $checkedConvert('lang', (v) => v as String?),
    tag: $checkedConvert('tag', (v) => v as String?),
    silent: $checkedConvert('silent', (v) => v as bool?),
    data: $checkedConvert(
      'data',
      (v) => v == null ? null : PushPayload.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$DeclarativePushPayloadNotificationToJson(
  DeclarativePushPayloadNotification instance,
) => <String, dynamic>{
  'title': instance.title,
  'body': ?instance.body,
  'navigate': instance.navigate,
  'lang': ?instance.lang,
  'tag': ?instance.tag,
  'silent': ?instance.silent,
  'data': ?instance.data?.toJson(),
};
