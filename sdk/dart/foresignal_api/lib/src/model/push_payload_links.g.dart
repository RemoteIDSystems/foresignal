// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'push_payload_links.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PushPayloadLinksCWProxy {
  PushPayloadLinks api(String? api);

  PushPayloadLinks app(String? app);

  PushPayloadLinks web(String? web);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushPayloadLinks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushPayloadLinks(...).copyWith(id: 12, name: "My name")
  /// ```
  PushPayloadLinks call({String? api, String? app, String? web});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPushPayloadLinks.copyWith(...)` or call `instanceOfPushPayloadLinks.copyWith.fieldName(value)` for a single field.
class _$PushPayloadLinksCWProxyImpl implements _$PushPayloadLinksCWProxy {
  const _$PushPayloadLinksCWProxyImpl(this._value);

  final PushPayloadLinks _value;

  @override
  PushPayloadLinks api(String? api) => call(api: api);

  @override
  PushPayloadLinks app(String? app) => call(app: app);

  @override
  PushPayloadLinks web(String? web) => call(web: web);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushPayloadLinks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushPayloadLinks(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PushPayloadLinks call({
    Object? api = const $CopyWithPlaceholder(),
    Object? app = const $CopyWithPlaceholder(),
    Object? web = const $CopyWithPlaceholder(),
  }) {
    return PushPayloadLinks(
      api: api == const $CopyWithPlaceholder()
          ? _value.api
          // ignore: cast_nullable_to_non_nullable
          : api as String?,
      app: app == const $CopyWithPlaceholder()
          ? _value.app
          // ignore: cast_nullable_to_non_nullable
          : app as String?,
      web: web == const $CopyWithPlaceholder()
          ? _value.web
          // ignore: cast_nullable_to_non_nullable
          : web as String?,
    );
  }
}

extension $PushPayloadLinksCopyWith on PushPayloadLinks {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPushPayloadLinks.copyWith(...)` or `instanceOfPushPayloadLinks.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PushPayloadLinksCWProxy get copyWith => _$PushPayloadLinksCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PushPayloadLinks _$PushPayloadLinksFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PushPayloadLinks', json, ($checkedConvert) {
      final val = PushPayloadLinks(
        api: $checkedConvert('api', (v) => v as String?),
        app: $checkedConvert('app', (v) => v as String?),
        web: $checkedConvert('web', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$PushPayloadLinksToJson(PushPayloadLinks instance) =>
    <String, dynamic>{
      'api': ?instance.api,
      'app': ?instance.app,
      'web': ?instance.web,
    };
