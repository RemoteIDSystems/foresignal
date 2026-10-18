//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'client_links.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ClientLinks {
  /// Returns a new [ClientLinks] instance.
  ClientLinks({

     this.alert = 'foresignal://alerts/{id}',

     this.record = 'foresignal://records/{source}/{externalId}',

     this.briefingRun = 'foresignal://briefings/{briefingId}/runs/{id}',

     this.pair = 'foresignal://pair?u={publicBaseUrl}&c={code}',

     this.share,
  });

  @JsonKey(
    defaultValue: 'foresignal://alerts/{id}',
    name: r'alert',
    required: false,
    includeIfNull: false,
  )


  final String? alert;



  @JsonKey(
    defaultValue: 'foresignal://records/{source}/{externalId}',
    name: r'record',
    required: false,
    includeIfNull: false,
  )


  final String? record;



  @JsonKey(
    defaultValue: 'foresignal://briefings/{briefingId}/runs/{id}',
    name: r'briefingRun',
    required: false,
    includeIfNull: false,
  )


  final String? briefingRun;



  @JsonKey(
    defaultValue: 'foresignal://pair?u={publicBaseUrl}&c={code}',
    name: r'pair',
    required: false,
    includeIfNull: false,
  )


  final String? pair;



      /// A share link opened in a client (112); placeholder {key}. No default: `?open=1` on a share-link URL redirects only when this is set.
  @JsonKey(
    
    name: r'share',
    required: false,
    includeIfNull: false,
  )


  final String? share;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ClientLinks &&
      other.alert == alert &&
      other.record == record &&
      other.briefingRun == briefingRun &&
      other.pair == pair &&
      other.share == share;

    @override
    int get hashCode =>
        alert.hashCode +
        record.hashCode +
        briefingRun.hashCode +
        pair.hashCode +
        (share == null ? 0 : share.hashCode);

  factory ClientLinks.fromJson(Map<String, dynamic> json) => _$ClientLinksFromJson(json);

  Map<String, dynamic> toJson() => _$ClientLinksToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

