//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'access_request_offer_share.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AccessRequestOfferShare {
  /// Returns a new [AccessRequestOfferShare] instance.
  AccessRequestOfferShare({

    required  this.chronicleId,
  });

  @JsonKey(
    
    name: r'chronicleId',
    required: true,
    includeIfNull: false,
  )


  final String chronicleId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AccessRequestOfferShare &&
      other.chronicleId == chronicleId;

    @override
    int get hashCode =>
        chronicleId.hashCode;

  factory AccessRequestOfferShare.fromJson(Map<String, dynamic> json) => _$AccessRequestOfferShareFromJson(json);

  Map<String, dynamic> toJson() => _$AccessRequestOfferShareToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

