// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_info_streams.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemInfoStreamsCWProxy {
  SystemInfoStreams clients(int? clients);

  SystemInfoStreams maxClients(int? maxClients);

  SystemInfoStreams changeSubscribers(int? changeSubscribers);

  SystemInfoStreams alertSubscribers(int? alertSubscribers);

  SystemInfoStreams eventSubscribers(int? eventSubscribers);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoStreams(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoStreams(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemInfoStreams call({
    int? clients,
    int? maxClients,
    int? changeSubscribers,
    int? alertSubscribers,
    int? eventSubscribers,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemInfoStreams.copyWith(...)` or call `instanceOfSystemInfoStreams.copyWith.fieldName(value)` for a single field.
class _$SystemInfoStreamsCWProxyImpl implements _$SystemInfoStreamsCWProxy {
  const _$SystemInfoStreamsCWProxyImpl(this._value);

  final SystemInfoStreams _value;

  @override
  SystemInfoStreams clients(int? clients) => call(clients: clients);

  @override
  SystemInfoStreams maxClients(int? maxClients) => call(maxClients: maxClients);

  @override
  SystemInfoStreams changeSubscribers(int? changeSubscribers) =>
      call(changeSubscribers: changeSubscribers);

  @override
  SystemInfoStreams alertSubscribers(int? alertSubscribers) =>
      call(alertSubscribers: alertSubscribers);

  @override
  SystemInfoStreams eventSubscribers(int? eventSubscribers) =>
      call(eventSubscribers: eventSubscribers);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoStreams(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoStreams(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemInfoStreams call({
    Object? clients = const $CopyWithPlaceholder(),
    Object? maxClients = const $CopyWithPlaceholder(),
    Object? changeSubscribers = const $CopyWithPlaceholder(),
    Object? alertSubscribers = const $CopyWithPlaceholder(),
    Object? eventSubscribers = const $CopyWithPlaceholder(),
  }) {
    return SystemInfoStreams(
      clients: clients == const $CopyWithPlaceholder()
          ? _value.clients
          // ignore: cast_nullable_to_non_nullable
          : clients as int?,
      maxClients: maxClients == const $CopyWithPlaceholder()
          ? _value.maxClients
          // ignore: cast_nullable_to_non_nullable
          : maxClients as int?,
      changeSubscribers: changeSubscribers == const $CopyWithPlaceholder()
          ? _value.changeSubscribers
          // ignore: cast_nullable_to_non_nullable
          : changeSubscribers as int?,
      alertSubscribers: alertSubscribers == const $CopyWithPlaceholder()
          ? _value.alertSubscribers
          // ignore: cast_nullable_to_non_nullable
          : alertSubscribers as int?,
      eventSubscribers: eventSubscribers == const $CopyWithPlaceholder()
          ? _value.eventSubscribers
          // ignore: cast_nullable_to_non_nullable
          : eventSubscribers as int?,
    );
  }
}

extension $SystemInfoStreamsCopyWith on SystemInfoStreams {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemInfoStreams.copyWith(...)` or `instanceOfSystemInfoStreams.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemInfoStreamsCWProxy get copyWith =>
      _$SystemInfoStreamsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemInfoStreams _$SystemInfoStreamsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SystemInfoStreams', json, ($checkedConvert) {
      final val = SystemInfoStreams(
        clients: $checkedConvert('clients', (v) => (v as num?)?.toInt()),
        maxClients: $checkedConvert('maxClients', (v) => (v as num?)?.toInt()),
        changeSubscribers: $checkedConvert(
          'changeSubscribers',
          (v) => (v as num?)?.toInt(),
        ),
        alertSubscribers: $checkedConvert(
          'alertSubscribers',
          (v) => (v as num?)?.toInt(),
        ),
        eventSubscribers: $checkedConvert(
          'eventSubscribers',
          (v) => (v as num?)?.toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SystemInfoStreamsToJson(SystemInfoStreams instance) =>
    <String, dynamic>{
      'clients': ?instance.clients,
      'maxClients': ?instance.maxClients,
      'changeSubscribers': ?instance.changeSubscribers,
      'alertSubscribers': ?instance.alertSubscribers,
      'eventSubscribers': ?instance.eventSubscribers,
    };
