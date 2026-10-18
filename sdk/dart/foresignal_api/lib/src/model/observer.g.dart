// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'observer.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ObserverCWProxy {
  Observer receiverId(String receiverId);

  Observer frequencyHz(int? frequencyHz);

  Observer rssi(num? rssi);

  Observer snr(num? snr);

  Observer decodeQuality(num? decodeQuality);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Observer(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Observer(...).copyWith(id: 12, name: "My name")
  /// ```
  Observer call({
    String receiverId,
    int? frequencyHz,
    num? rssi,
    num? snr,
    num? decodeQuality,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfObserver.copyWith(...)` or call `instanceOfObserver.copyWith.fieldName(value)` for a single field.
class _$ObserverCWProxyImpl implements _$ObserverCWProxy {
  const _$ObserverCWProxyImpl(this._value);

  final Observer _value;

  @override
  Observer receiverId(String receiverId) => call(receiverId: receiverId);

  @override
  Observer frequencyHz(int? frequencyHz) => call(frequencyHz: frequencyHz);

  @override
  Observer rssi(num? rssi) => call(rssi: rssi);

  @override
  Observer snr(num? snr) => call(snr: snr);

  @override
  Observer decodeQuality(num? decodeQuality) =>
      call(decodeQuality: decodeQuality);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Observer(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Observer(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Observer call({
    Object? receiverId = const $CopyWithPlaceholder(),
    Object? frequencyHz = const $CopyWithPlaceholder(),
    Object? rssi = const $CopyWithPlaceholder(),
    Object? snr = const $CopyWithPlaceholder(),
    Object? decodeQuality = const $CopyWithPlaceholder(),
  }) {
    return Observer(
      receiverId:
          receiverId == const $CopyWithPlaceholder() || receiverId == null
          ? _value.receiverId
          // ignore: cast_nullable_to_non_nullable
          : receiverId as String,
      frequencyHz: frequencyHz == const $CopyWithPlaceholder()
          ? _value.frequencyHz
          // ignore: cast_nullable_to_non_nullable
          : frequencyHz as int?,
      rssi: rssi == const $CopyWithPlaceholder()
          ? _value.rssi
          // ignore: cast_nullable_to_non_nullable
          : rssi as num?,
      snr: snr == const $CopyWithPlaceholder()
          ? _value.snr
          // ignore: cast_nullable_to_non_nullable
          : snr as num?,
      decodeQuality: decodeQuality == const $CopyWithPlaceholder()
          ? _value.decodeQuality
          // ignore: cast_nullable_to_non_nullable
          : decodeQuality as num?,
    );
  }
}

extension $ObserverCopyWith on Observer {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfObserver.copyWith(...)` or `instanceOfObserver.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ObserverCWProxy get copyWith => _$ObserverCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Observer _$ObserverFromJson(Map<String, dynamic> json) => $checkedCreate(
  'Observer',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['receiverId']);
    final val = Observer(
      receiverId: $checkedConvert('receiverId', (v) => v as String),
      frequencyHz: $checkedConvert('frequencyHz', (v) => (v as num?)?.toInt()),
      rssi: $checkedConvert('rssi', (v) => v as num?),
      snr: $checkedConvert('snr', (v) => v as num?),
      decodeQuality: $checkedConvert('decodeQuality', (v) => v as num?),
    );
    return val;
  },
);

Map<String, dynamic> _$ObserverToJson(Observer instance) => <String, dynamic>{
  'receiverId': instance.receiverId,
  'frequencyHz': ?instance.frequencyHz,
  'rssi': ?instance.rssi,
  'snr': ?instance.snr,
  'decodeQuality': ?instance.decodeQuality,
};
