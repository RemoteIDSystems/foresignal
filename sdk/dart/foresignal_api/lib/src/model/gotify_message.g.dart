// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'gotify_message.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GotifyMessageCWProxy {
  GotifyMessage id(int id);

  GotifyMessage appid(GotifyMessageAppidEnum appid);

  GotifyMessage date(DateTime date);

  GotifyMessage title(String? title);

  GotifyMessage message(String message);

  GotifyMessage priority(int? priority);

  GotifyMessage extras(Map<String, Object>? extras);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GotifyMessage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GotifyMessage(...).copyWith(id: 12, name: "My name")
  /// ```
  GotifyMessage call({
    int id,
    GotifyMessageAppidEnum appid,
    DateTime date,
    String? title,
    String message,
    int? priority,
    Map<String, Object>? extras,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGotifyMessage.copyWith(...)` or call `instanceOfGotifyMessage.copyWith.fieldName(value)` for a single field.
class _$GotifyMessageCWProxyImpl implements _$GotifyMessageCWProxy {
  const _$GotifyMessageCWProxyImpl(this._value);

  final GotifyMessage _value;

  @override
  GotifyMessage id(int id) => call(id: id);

  @override
  GotifyMessage appid(GotifyMessageAppidEnum appid) => call(appid: appid);

  @override
  GotifyMessage date(DateTime date) => call(date: date);

  @override
  GotifyMessage title(String? title) => call(title: title);

  @override
  GotifyMessage message(String message) => call(message: message);

  @override
  GotifyMessage priority(int? priority) => call(priority: priority);

  @override
  GotifyMessage extras(Map<String, Object>? extras) => call(extras: extras);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GotifyMessage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GotifyMessage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  GotifyMessage call({
    Object? id = const $CopyWithPlaceholder(),
    Object? appid = const $CopyWithPlaceholder(),
    Object? date = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? priority = const $CopyWithPlaceholder(),
    Object? extras = const $CopyWithPlaceholder(),
  }) {
    return GotifyMessage(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as int,
      appid: appid == const $CopyWithPlaceholder() || appid == null
          ? _value.appid
          // ignore: cast_nullable_to_non_nullable
          : appid as GotifyMessageAppidEnum,
      date: date == const $CopyWithPlaceholder() || date == null
          ? _value.date
          // ignore: cast_nullable_to_non_nullable
          : date as DateTime,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      message: message == const $CopyWithPlaceholder() || message == null
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String,
      priority: priority == const $CopyWithPlaceholder()
          ? _value.priority
          // ignore: cast_nullable_to_non_nullable
          : priority as int?,
      extras: extras == const $CopyWithPlaceholder()
          ? _value.extras
          // ignore: cast_nullable_to_non_nullable
          : extras as Map<String, Object>?,
    );
  }
}

extension $GotifyMessageCopyWith on GotifyMessage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGotifyMessage.copyWith(...)` or `instanceOfGotifyMessage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GotifyMessageCWProxy get copyWith => _$GotifyMessageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GotifyMessage _$GotifyMessageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('GotifyMessage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'appid', 'date', 'message']);
      final val = GotifyMessage(
        id: $checkedConvert('id', (v) => (v as num).toInt()),
        appid: $checkedConvert(
          'appid',
          (v) => $enumDecode(_$GotifyMessageAppidEnumEnumMap, v),
        ),
        date: $checkedConvert('date', (v) => DateTime.parse(v as String)),
        title: $checkedConvert('title', (v) => v as String?),
        message: $checkedConvert('message', (v) => v as String),
        priority: $checkedConvert('priority', (v) => (v as num?)?.toInt()),
        extras: $checkedConvert(
          'extras',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$GotifyMessageToJson(GotifyMessage instance) =>
    <String, dynamic>{
      'id': instance.id,
      'appid': _$GotifyMessageAppidEnumEnumMap[instance.appid]!,
      'date': instance.date.toIso8601String(),
      'title': ?instance.title,
      'message': instance.message,
      'priority': ?instance.priority,
      'extras': ?instance.extras,
    };

const _$GotifyMessageAppidEnumEnumMap = {GotifyMessageAppidEnum.number1: 1};
