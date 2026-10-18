// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'custom_source_formats_alertmanager.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CustomSourceFormatsAlertmanagerCWProxy {
  CustomSourceFormatsAlertmanager kindLabel(String? kindLabel);

  CustomSourceFormatsAlertmanager resolved(
    CustomSourceFormatsAlertmanagerResolvedEnum? resolved,
  );

  CustomSourceFormatsAlertmanager receiverPrefix(bool? receiverPrefix);

  CustomSourceFormatsAlertmanager resolvedValidFor(String? resolvedValidFor);

  CustomSourceFormatsAlertmanager station(List<num>? station);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CustomSourceFormatsAlertmanager(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CustomSourceFormatsAlertmanager(...).copyWith(id: 12, name: "My name")
  /// ```
  CustomSourceFormatsAlertmanager call({
    String? kindLabel,
    CustomSourceFormatsAlertmanagerResolvedEnum? resolved,
    bool? receiverPrefix,
    String? resolvedValidFor,
    List<num>? station,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCustomSourceFormatsAlertmanager.copyWith(...)` or call `instanceOfCustomSourceFormatsAlertmanager.copyWith.fieldName(value)` for a single field.
class _$CustomSourceFormatsAlertmanagerCWProxyImpl
    implements _$CustomSourceFormatsAlertmanagerCWProxy {
  const _$CustomSourceFormatsAlertmanagerCWProxyImpl(this._value);

  final CustomSourceFormatsAlertmanager _value;

  @override
  CustomSourceFormatsAlertmanager kindLabel(String? kindLabel) =>
      call(kindLabel: kindLabel);

  @override
  CustomSourceFormatsAlertmanager resolved(
    CustomSourceFormatsAlertmanagerResolvedEnum? resolved,
  ) => call(resolved: resolved);

  @override
  CustomSourceFormatsAlertmanager receiverPrefix(bool? receiverPrefix) =>
      call(receiverPrefix: receiverPrefix);

  @override
  CustomSourceFormatsAlertmanager resolvedValidFor(String? resolvedValidFor) =>
      call(resolvedValidFor: resolvedValidFor);

  @override
  CustomSourceFormatsAlertmanager station(List<num>? station) =>
      call(station: station);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CustomSourceFormatsAlertmanager(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CustomSourceFormatsAlertmanager(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CustomSourceFormatsAlertmanager call({
    Object? kindLabel = const $CopyWithPlaceholder(),
    Object? resolved = const $CopyWithPlaceholder(),
    Object? receiverPrefix = const $CopyWithPlaceholder(),
    Object? resolvedValidFor = const $CopyWithPlaceholder(),
    Object? station = const $CopyWithPlaceholder(),
  }) {
    return CustomSourceFormatsAlertmanager(
      kindLabel: kindLabel == const $CopyWithPlaceholder()
          ? _value.kindLabel
          // ignore: cast_nullable_to_non_nullable
          : kindLabel as String?,
      resolved: resolved == const $CopyWithPlaceholder()
          ? _value.resolved
          // ignore: cast_nullable_to_non_nullable
          : resolved as CustomSourceFormatsAlertmanagerResolvedEnum?,
      receiverPrefix: receiverPrefix == const $CopyWithPlaceholder()
          ? _value.receiverPrefix
          // ignore: cast_nullable_to_non_nullable
          : receiverPrefix as bool?,
      resolvedValidFor: resolvedValidFor == const $CopyWithPlaceholder()
          ? _value.resolvedValidFor
          // ignore: cast_nullable_to_non_nullable
          : resolvedValidFor as String?,
      station: station == const $CopyWithPlaceholder()
          ? _value.station
          // ignore: cast_nullable_to_non_nullable
          : station as List<num>?,
    );
  }
}

extension $CustomSourceFormatsAlertmanagerCopyWith
    on CustomSourceFormatsAlertmanager {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCustomSourceFormatsAlertmanager.copyWith(...)` or `instanceOfCustomSourceFormatsAlertmanager.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CustomSourceFormatsAlertmanagerCWProxy get copyWith =>
      _$CustomSourceFormatsAlertmanagerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CustomSourceFormatsAlertmanager _$CustomSourceFormatsAlertmanagerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CustomSourceFormatsAlertmanager', json, ($checkedConvert) {
  final val = CustomSourceFormatsAlertmanager(
    kindLabel: $checkedConvert('kindLabel', (v) => v as String?),
    resolved: $checkedConvert(
      'resolved',
      (v) => $enumDecodeNullable(
        _$CustomSourceFormatsAlertmanagerResolvedEnumEnumMap,
        v,
      ),
    ),
    receiverPrefix: $checkedConvert(
      'receiverPrefix',
      (v) => v as bool? ?? false,
    ),
    resolvedValidFor: $checkedConvert('resolvedValidFor', (v) => v as String?),
    station: $checkedConvert(
      'station',
      (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$CustomSourceFormatsAlertmanagerToJson(
  CustomSourceFormatsAlertmanager instance,
) => <String, dynamic>{
  'kindLabel': ?instance.kindLabel,
  'resolved':
      ?_$CustomSourceFormatsAlertmanagerResolvedEnumEnumMap[instance.resolved],
  'receiverPrefix': ?instance.receiverPrefix,
  'resolvedValidFor': ?instance.resolvedValidFor,
  'station': ?instance.station,
};

const _$CustomSourceFormatsAlertmanagerResolvedEnumEnumMap = {
  CustomSourceFormatsAlertmanagerResolvedEnum.tombstone: 'tombstone',
  CustomSourceFormatsAlertmanagerResolvedEnum.record: 'record',
};
