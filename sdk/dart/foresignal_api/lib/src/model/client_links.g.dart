// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'client_links.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ClientLinksCWProxy {
  ClientLinks alert(String? alert);

  ClientLinks record(String? record);

  ClientLinks briefingRun(String? briefingRun);

  ClientLinks pair(String? pair);

  ClientLinks share(String? share);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ClientLinks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ClientLinks(...).copyWith(id: 12, name: "My name")
  /// ```
  ClientLinks call({
    String? alert,
    String? record,
    String? briefingRun,
    String? pair,
    String? share,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfClientLinks.copyWith(...)` or call `instanceOfClientLinks.copyWith.fieldName(value)` for a single field.
class _$ClientLinksCWProxyImpl implements _$ClientLinksCWProxy {
  const _$ClientLinksCWProxyImpl(this._value);

  final ClientLinks _value;

  @override
  ClientLinks alert(String? alert) => call(alert: alert);

  @override
  ClientLinks record(String? record) => call(record: record);

  @override
  ClientLinks briefingRun(String? briefingRun) =>
      call(briefingRun: briefingRun);

  @override
  ClientLinks pair(String? pair) => call(pair: pair);

  @override
  ClientLinks share(String? share) => call(share: share);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ClientLinks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ClientLinks(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ClientLinks call({
    Object? alert = const $CopyWithPlaceholder(),
    Object? record = const $CopyWithPlaceholder(),
    Object? briefingRun = const $CopyWithPlaceholder(),
    Object? pair = const $CopyWithPlaceholder(),
    Object? share = const $CopyWithPlaceholder(),
  }) {
    return ClientLinks(
      alert: alert == const $CopyWithPlaceholder()
          ? _value.alert
          // ignore: cast_nullable_to_non_nullable
          : alert as String?,
      record: record == const $CopyWithPlaceholder()
          ? _value.record
          // ignore: cast_nullable_to_non_nullable
          : record as String?,
      briefingRun: briefingRun == const $CopyWithPlaceholder()
          ? _value.briefingRun
          // ignore: cast_nullable_to_non_nullable
          : briefingRun as String?,
      pair: pair == const $CopyWithPlaceholder()
          ? _value.pair
          // ignore: cast_nullable_to_non_nullable
          : pair as String?,
      share: share == const $CopyWithPlaceholder()
          ? _value.share
          // ignore: cast_nullable_to_non_nullable
          : share as String?,
    );
  }
}

extension $ClientLinksCopyWith on ClientLinks {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfClientLinks.copyWith(...)` or `instanceOfClientLinks.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ClientLinksCWProxy get copyWith => _$ClientLinksCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ClientLinks _$ClientLinksFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ClientLinks', json, ($checkedConvert) {
      final val = ClientLinks(
        alert: $checkedConvert(
          'alert',
          (v) => v as String? ?? 'foresignal://alerts/{id}',
        ),
        record: $checkedConvert(
          'record',
          (v) => v as String? ?? 'foresignal://records/{source}/{externalId}',
        ),
        briefingRun: $checkedConvert(
          'briefingRun',
          (v) =>
              v as String? ?? 'foresignal://briefings/{briefingId}/runs/{id}',
        ),
        pair: $checkedConvert(
          'pair',
          (v) => v as String? ?? 'foresignal://pair?u={publicBaseUrl}&c={code}',
        ),
        share: $checkedConvert('share', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ClientLinksToJson(ClientLinks instance) =>
    <String, dynamic>{
      'alert': ?instance.alert,
      'record': ?instance.record,
      'briefingRun': ?instance.briefingRun,
      'pair': ?instance.pair,
      'share': ?instance.share,
    };
